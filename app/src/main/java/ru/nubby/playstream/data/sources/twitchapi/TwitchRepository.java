package ru.nubby.playstream.data.sources.twitchapi;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.subjects.BehaviorSubject;
import ru.nubby.playstream.data.sources.twitchapi.services.RawJsonService;
import ru.nubby.playstream.data.sources.twitchapi.services.TwitchApiService;
import ru.nubby.playstream.data.sources.twitchapi.services.TwitchHelixService;
import ru.nubby.playstream.data.sources.twitchapi.services.TwitchKrakenService;
import ru.nubby.playstream.domain.entities.FollowRelations;
import ru.nubby.playstream.domain.entities.Pagination;
import ru.nubby.playstream.domain.entities.Quality;
import ru.nubby.playstream.domain.entities.Stream;
import ru.nubby.playstream.domain.entities.StreamToken;
import ru.nubby.playstream.domain.entities.StreamsResponse;
import ru.nubby.playstream.domain.entities.UserData;
import ru.nubby.playstream.domain.entities.UserDataResponse;
import ru.nubby.playstream.domain.entities.UserFollowsResponse;
import ru.nubby.playstream.utils.M3U8Parser;
import ru.nubby.playstream.utils.RxSchedulersProvider;

@Singleton
public class TwitchRepository implements RemoteRepository {

    private final String TAG = TwitchRepository.class.getSimpleName();

    private final RawJsonService mRawJsonService;
    private final TwitchApiService mTwitchApiService;
    private final TwitchKrakenService mTwitchKrakenService;
    private final TwitchHelixService mTwitchHelixService;

    private final Scheduler mIoScheduler;
    private final Scheduler mComputationScheduler;

    @Inject
    public TwitchRepository(RawJsonService rawJsonService,
                            TwitchApiService twitchApiService,
                            TwitchKrakenService twitchKrakenService,
                            TwitchHelixService twitchHelixService,
                            RxSchedulersProvider rxSchedulersProvider) {
        mRawJsonService = rawJsonService;
        mTwitchApiService = twitchApiService;
        mTwitchKrakenService = twitchKrakenService;
        mTwitchHelixService = twitchHelixService;
        mIoScheduler = rxSchedulersProvider.getIoScheduler();
        mComputationScheduler = rxSchedulersProvider.getComputationScheduler();
    }

    @Override
    public Single<HashMap<Quality, String>> getQualityUrls(Stream stream) {
        Single<String> channelName;

        if (stream.getStreamerLogin() != null && !stream.getStreamerLogin().isEmpty()) {
            channelName = Single
                    .just(stream.getStreamerLogin())
                    .subscribeOn(mIoScheduler);
        } else {
            channelName = getStreamerInfo(stream)
                    .map(UserData::getLogin);
        }

        Single<StreamToken> tokenSingle = channelName
                .flatMap(channelNameString -> mTwitchApiService
                        .getAccessToken(channelNameString.toLowerCase())
                        .subscribeOn(mIoScheduler));

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
                        .subscribeOn(mIoScheduler)
                        .observeOn(mComputationScheduler)
                        .map(M3U8Parser::parseTwitchApiResponse));
    }

    @Override
    public Single<UserData> getStreamerInfo(Stream stream) {

        List<String> oneElementList = new ArrayList<>();
        oneElementList.add(stream.getUserId());

        return mTwitchHelixService
                .getUserDataByIds(oneElementList)
                .subscribeOn(mIoScheduler)
                .filter(userDataResponse -> !userDataResponse.getData().isEmpty())
                .map(userDataResponse -> userDataResponse.getData().get(0))
                .toSingle();
    }

    @Override
    public Single<List<UserData>> getUserDataListByStreamList(List<Stream> streamIdList) {
        return Observable
                .fromIterable(streamIdList)
                .subscribeOn(mComputationScheduler)
                .map(Stream::getUserId)
                .toList()
                .flatMap(this::getUserDataListByStringIds);
    }

    @Override
    public Single<List<UserData>> getUpdatedUserDataList(List<UserData> userDataList) {
        return Observable
                .fromIterable(userDataList)
                .subscribeOn(mComputationScheduler)
                .map(UserData::getId)
                .toList()
                .flatMap(this::getUserDataListByStringIds);
    }

    @Override
    public Single<UserData> getUserDataFromToken(String token) {
        return mTwitchHelixService
                .getUserDataByToken("Bearer " + token)
                .subscribeOn(mIoScheduler)
                .filter(userDataResponse -> !userDataResponse.getData().isEmpty())
                .map(userDataResponse -> userDataResponse.getData().get(0))
                .toSingle();
    }

    @Override
    public Observable<Stream> getUpdatedStreamInfo(Stream stream) {

        List<String> oneElementList = new ArrayList<>();
        oneElementList.add(stream.getUserId());

        return mTwitchHelixService
                .getAllStreamsByUserList(oneElementList)
                .subscribeOn(mIoScheduler)
                .map(streamsResponse -> streamsResponse.getData().get(0))
                .delay(30, TimeUnit.SECONDS)
                .repeat()
                .toObservable();
    }

    @Override
    public Single<StreamsResponse> getTopStreams() {
        return mTwitchHelixService
                .getTopStreams(null)
                .subscribeOn(mIoScheduler);
    }

    @Override
    public Single<StreamsResponse> getTopStreams(Pagination pagination) {
        return mTwitchHelixService
                .getTopStreams(pagination.getCursor())
                .subscribeOn(mIoScheduler);
    }

    @Override
    public Single<List<FollowRelations>> getUserFollows(String userId) {

        return  getAllUserFollowRelations(userId)
                .toList()
                .subscribeOn(mIoScheduler);
    }

    @Override
    public Single<List<Stream>> getLiveStreamsFollowedByUser(String userId) {
        return getAllUserFollowRelations(userId)
                .subscribeOn(mIoScheduler)
                .map(FollowRelations::getToId)
                .buffer(100)
                .flatMap(userList -> mTwitchHelixService
                        .getAllStreamsByUserList(userList)
                        .toObservable())
                .subscribeOn(mComputationScheduler)
                .map(StreamsResponse::getData)
                .flatMap(Observable::fromIterable)
                .toSortedList();
    }

    @Override
    public Single<List<Stream>> getLiveStreamsFromRelationList(
            Single<List<FollowRelations>> singleFollowRelationsList) {
        return singleFollowRelationsList
                .subscribeOn(mComputationScheduler)
                .toObservable()
                .flatMap(Observable::fromIterable)
                .map(FollowRelations::getToId)
                .buffer(100)
                .subscribeOn(mIoScheduler)
                .flatMap(userList -> mTwitchHelixService
                        .getAllStreamsByUserList(userList)
                        .toObservable())
                .subscribeOn(mComputationScheduler)
                .map(StreamsResponse::getData)
                .flatMap(Observable::fromIterable)
                .toSortedList();
    }

    @Override
    public Completable followTargetUser(String token, String userId, String targetUserId){
        return mTwitchKrakenService
                .followTargetUser(userId, targetUserId, token)
                .subscribeOn(mIoScheduler);
    }

    @Override
    public Completable unfollowTargetUser(String token, String userId, String targetUserId){
        return mTwitchKrakenService
                .unfollowTargetUser(userId, targetUserId, token)
                .subscribeOn(mIoScheduler);
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
                                    .getUserFollowsById(userId, null)
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
                            return Observable.<UserFollowsResponse>empty()
                                    .doOnComplete(pagecontrol::onComplete);
                        }
                    });
                })
                .map(UserFollowsResponse::getData)
                .flatMap(Observable::fromIterable);
    }

    private Single<List<UserData>> getUserDataListByStringIds(List<String> idList) {
        return Observable
                .fromIterable(idList)
                .subscribeOn(mComputationScheduler)
                .buffer(100)
                .subscribeOn(mIoScheduler)
                .flatMap(idListBuffered -> mTwitchHelixService
                        .getUserDataByIds(idListBuffered)
                        .map(UserDataResponse::getData)
                        .subscribeOn(mIoScheduler)
                        .toObservable())
                .subscribeOn(mComputationScheduler)
                .flatMap(Observable::fromIterable)
                .toList();
    }
}
