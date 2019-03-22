package ru.nubby.playstream.data.twitchapi;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import ru.nubby.playstream.data.twitchapi.services.RawJsonService;
import ru.nubby.playstream.data.twitchapi.services.TwitchApiService;
import ru.nubby.playstream.data.twitchapi.services.TwitchHelixService;
import ru.nubby.playstream.data.twitchapi.services.TwitchKrakenService;
import ru.nubby.playstream.model.FollowRelations;
import ru.nubby.playstream.model.Pagination;
import ru.nubby.playstream.model.Quality;
import ru.nubby.playstream.model.Stream;
import ru.nubby.playstream.model.StreamToken;
import ru.nubby.playstream.model.StreamsRequest;
import ru.nubby.playstream.model.UserData;
import ru.nubby.playstream.model.UserDataList;
import ru.nubby.playstream.model.UserFollowsRequest;
import ru.nubby.playstream.utils.M3U8Parser;

/**
 * //todo make interface contract for that class.
 */
@Singleton
public class RemoteRepository {

    private final String TAG = RemoteRepository.class.getSimpleName();

    private final RawJsonService mRawJsonService;
    private final TwitchApiService mTwitchApiService;
    private final TwitchKrakenService mTwitchKrakenService;
    private final TwitchHelixService mTwitchHelixService;

    @Inject
    public RemoteRepository(RawJsonService rawJsonService,
                            TwitchApiService twitchApiService,
                            TwitchKrakenService twitchKrakenService,
                            TwitchHelixService twitchHelixService) {
        mRawJsonService = rawJsonService;
        mTwitchApiService = twitchApiService;
        mTwitchKrakenService = twitchKrakenService;
        mTwitchHelixService = twitchHelixService;
    }

    public Single<HashMap<Quality, String>> getQualityUrls(Stream stream) {
        Single<String> channelName;

        if (stream.getStreamerLogin() != null && !stream.getStreamerLogin().equals("")) {
            channelName = Single
                    .just(stream.getStreamerLogin())
                    .subscribeOn(Schedulers.io());
        } else {
            channelName = getStreamerInfo(stream)
                    .map(UserData::getLogin);
        }

        Single<StreamToken> tokenSingle = channelName
                .flatMap(channelNameString -> mTwitchApiService
                        .getAccessToken(channelNameString.toLowerCase())
                        .subscribeOn(Schedulers.io()));

        return Single
                .zip(tokenSingle, channelName,
                        (streamToken, streamerName) ->
                                String.format("%s.m3u8" +
                                                "?token=%s" +
                                                "&sig=%s" +
                                                "&player=twitchweb" +
                                                "&allow_audio_only=true" +
                                                "&allow_source=true" +
                                                "&type=any" +
                                                "&p=%s",
                                        streamerName,
                                        URLEncoder.encode(streamToken.getToken(), "UTF-8")
                                                .replaceAll("%3A", ":")
                                                .replaceAll("%2C", ","),
                                        streamToken.getSig(),
                                        "" + new Random().nextInt(6)))
                .flatMap(urlToGetStreamPlaylist -> mRawJsonService
                        .getRawJsonFromPath(urlToGetStreamPlaylist)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.computation())
                        .map(M3U8Parser::parseTwitchApiResponse)
                        .observeOn(AndroidSchedulers.mainThread()));
    }

    public Single<UserData> getStreamerInfo(Stream stream) {
        return mTwitchHelixService
                .getUserDataListById(stream.getUserId())
                .subscribeOn(Schedulers.io())
                .filter(userDataList -> !userDataList.getData().isEmpty())
                .map(userDataList -> userDataList.getData().get(0))
                .toSingle();
    }

    public Single<List<UserData>> getUserDataListByStreamList(List<Stream> streamIdList) {
        return Observable
                .fromIterable(streamIdList)
                .subscribeOn(Schedulers.computation())
                .map(Stream::getUserId)
                .buffer(100)
                .subscribeOn(Schedulers.io())
                .flatMap(idList -> mTwitchHelixService
                        .getUserDataListByIdsList(idList)
                        .map(UserDataList::getData)
                        .subscribeOn(Schedulers.io())
                        .toObservable())
                .subscribeOn(Schedulers.computation())
                .flatMap(Observable::fromIterable)
                .toList();
    }

    public Single<UserData> getUserDataFromToken(String token) {
        return mTwitchHelixService
                .getUserDataListByToken("Bearer " + token)
                .subscribeOn(Schedulers.io())
                .filter(userDataList -> !userDataList.getData().isEmpty())
                .map(userDataList -> userDataList.getData().get(0))
                .toSingle();
    }

    public Observable<Stream> getUpdatedStreamInfo(Stream stream) {
        return mTwitchHelixService
                .updateStream(stream.getUserId())
                .subscribeOn(Schedulers.io())
                .map(streamsRequest -> streamsRequest.getData().get(0))
                .delay(30, TimeUnit.SECONDS)
                .repeat()
                .toObservable();
    }

    public Single<StreamsRequest> getTopStreams() {
        return mTwitchHelixService
                .getTopStreams()
                .subscribeOn(Schedulers.io());
    }

    public Single<StreamsRequest> getTopStreams(Pagination pagination) {
        return mTwitchHelixService
                .getTopStreams(pagination.getCursor())
                .subscribeOn(Schedulers.io());
    }

    public Single<List<FollowRelations>> getUserFollows(String userId) {

        return  getAllUserFollowRelations(userId)
                .toList()
                .subscribeOn(Schedulers.io());
    }

    public Single<List<Stream>> getLiveStreamsFollowedByUser(String userId) {
        return getAllUserFollowRelations(userId)
                .subscribeOn(Schedulers.io())
                .map(FollowRelations::getToId)
                .buffer(100)
                .flatMap(userList -> mTwitchHelixService
                        .getAllStreamsByUserList(userList)
                        .toObservable())
                .subscribeOn(Schedulers.computation())
                .map(StreamsRequest::getData)
                .flatMap(Observable::fromIterable)
                .toSortedList();
    }

    public Single<List<Stream>> getLiveStreamsFromRelationList(
            Single<List<FollowRelations>> singleFollowRelationsList) {
        return singleFollowRelationsList
                .subscribeOn(Schedulers.computation())
                .toObservable()
                .flatMap(Observable::fromIterable)
                .map(FollowRelations::getToId)
                .buffer(100)
                .subscribeOn(Schedulers.io())
                .flatMap(userList -> mTwitchHelixService
                        .getAllStreamsByUserList(userList)
                        .toObservable())
                .subscribeOn(Schedulers.computation())
                .map(StreamsRequest::getData)
                .flatMap(Observable::fromIterable)
                .toSortedList();
    }

    public Completable followTargetUser(String token, String userId, String targetUserId){
        return mTwitchKrakenService
                .followTargetUser(userId, targetUserId, token)
                .subscribeOn(Schedulers.io());
    }

    public Completable unfollowTargetUser(String token, String userId, String targetUserId){
        return mTwitchKrakenService
                .unfollowTargetUser(userId, targetUserId, token)
                .subscribeOn(Schedulers.io());
    }

    private Observable<FollowRelations> getAllUserFollowRelations(String userId) {
        //Paginated request
        //first emit happens with "start" key
        //all further emits will happen with pagination string aKey
        return Observable
                .defer(() ->
                {
                    BehaviorSubject<String> pagecontrol = BehaviorSubject.create();
                    pagecontrol.onNext("start");
                    return pagecontrol.concatMap(aKey ->
                    {
                        if (aKey != null && aKey.equals("start")) {
                            return mTwitchHelixService
                                    .getUserFollowsById(userId)
                                    .doOnSuccess(page -> {
                                        if (page.getPagination() != null &&
                                                page.getPagination().getCursor() != null)
                                            pagecontrol.onNext(page.getPagination().getCursor());
                                        else pagecontrol.onComplete();
                                    })
                                    .toObservable();
                        } else if (aKey != null) {
                            return mTwitchHelixService
                                    .getUserFollowsById(userId, aKey)
                                    .doOnSuccess(page -> {
                                        if (page.getPagination() != null &&
                                                page.getPagination().getCursor() != null)
                                            pagecontrol.onNext(page.getPagination().getCursor());
                                        else pagecontrol.onComplete();
                                    })
                                    .toObservable();

                        } else {
                            return Observable.<UserFollowsRequest>empty()
                                    .doOnComplete(pagecontrol::onComplete);
                        }
                    });
                })
                .map(UserFollowsRequest::getData)
                .flatMap(Observable::fromIterable);
    }

}
