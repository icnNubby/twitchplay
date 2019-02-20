package ru.nubby.playstream.data.twitchapi;

import android.util.Log;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import ru.nubby.playstream.SensitiveStorage;
import ru.nubby.playstream.model.FollowRelations;
import ru.nubby.playstream.model.Pagination;
import ru.nubby.playstream.model.Quality;
import ru.nubby.playstream.model.Stream;
import ru.nubby.playstream.model.StreamToken;
import ru.nubby.playstream.model.StreamsRequest;
import ru.nubby.playstream.model.UserData;
import ru.nubby.playstream.model.UserFollowsRequest;
import ru.nubby.playstream.utils.M3U8Parser;

/**
 * ATM couples FullInfo and List logics.
 */
public class RemoteRepository {
    private final String TAG = RemoteRepository.class.getSimpleName();

    public Single<HashMap<Quality, String>> getVideoUrl(Stream stream) {
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
                .flatMap(channelNameString -> TwitchApi
                        .getInstance()
                        .getStreamApiService()
                        .getAccessToken(SensitiveStorage.getClientApiKey(),
                                channelNameString.toLowerCase())
                        .subscribeOn(Schedulers.io()));

        return Single
                .zip(tokenSingle, channelName,
                        (streamToken, streamerName) ->
                                String.format("%s.m3u8" +
                                                "?streamToken=%s" +
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
                .flatMap(urlToGetStreamPlaylist -> TwitchApi
                        .getInstance()
                        .getRawJsonHlsService()
                        .getRawJsonFromPath(SensitiveStorage.getClientApiKey(), urlToGetStreamPlaylist)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.computation())
                        .map(M3U8Parser::parseTwitchApiResponse)
                        .observeOn(AndroidSchedulers.mainThread()));
    }

    public Single<UserData> getStreamerInfo(Stream stream) {
        return TwitchApi
                .getInstance()
                .getStreamHelixService()
                .getUserDataListById(SensitiveStorage.getClientApiKey(),
                        stream.getUserId())
                .subscribeOn(Schedulers.io())
                .filter(userDataList -> !userDataList.getData().isEmpty())
                .map(userDataList -> userDataList.getData().get(0))
                .doOnSuccess(login -> Log.d(TAG, "Login is " + login))
                .doOnError(error -> Log.e(TAG, "Error while getting streamer info " + error, error))
                .toSingle()
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<UserData> getUserDataFromToken(String token) {
        return TwitchApi
                .getInstance()
                .getStreamHelixService()
                .getUserDataListByToken(SensitiveStorage.getClientApiKey(),
                        "Bearer " + token)
                .subscribeOn(Schedulers.io())
                .filter(userDataList -> !userDataList.getData().isEmpty())
                .map(userDataList -> userDataList.getData().get(0))
                .doOnSuccess(userData -> Log.d(TAG, "Login is " + userData.getLogin()))
                .doOnError(error -> Log.e(TAG, "Error while getting user info " + error, error))
                .toSingle()
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Stream> getUpdatedStreamInfo(Stream stream) {
        return TwitchApi
                .getInstance()
                .getStreamHelixService()
                .updateStream(SensitiveStorage.getClientApiKey(), stream.getUserId())
                .subscribeOn(Schedulers.io())
                .map(streamsRequest -> streamsRequest.getData().get(0))
                .delay(30, TimeUnit.SECONDS)
                .repeat()
                .toObservable()
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<StreamsRequest> getStreams() {
        return TwitchApi
                .getInstance()
                .getStreamHelixService()
                .getTopStreams(SensitiveStorage.getClientApiKey())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<StreamsRequest> getStreams(Pagination pagination) {
        return TwitchApi
                .getInstance()
                .getStreamHelixService()
                .getMoreStreamsAfter(SensitiveStorage.getClientApiKey(), pagination.getCursor())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
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
                .flatMap(userList -> TwitchApi
                        .getInstance()
                        .getStreamHelixService()
                        .getAllStreamsByUserList(SensitiveStorage.getClientApiKey(), userList)
                        .toObservable())
                .subscribeOn(Schedulers.computation())
                .map(StreamsRequest::getData)
                .flatMap(Observable::fromIterable)
                .toSortedList()
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<List<Stream>> getLiveStreamsFromRelationList(Single<List<FollowRelations>> singleFollowRelationsList) {
        return singleFollowRelationsList
                .subscribeOn(Schedulers.computation())
                .toObservable()
                .flatMap(Observable::fromIterable)
                .map(FollowRelations::getToId)
                .buffer(100)
                .subscribeOn(Schedulers.io())
                .flatMap(userList -> TwitchApi
                        .getInstance()
                        .getStreamHelixService()
                        .getAllStreamsByUserList(SensitiveStorage.getClientApiKey(), userList)
                        .toObservable())
                .subscribeOn(Schedulers.computation())
                .map(StreamsRequest::getData)
                .flatMap(Observable::fromIterable)
                .toSortedList()
                .observeOn(AndroidSchedulers.mainThread());
    }

    private Observable<FollowRelations> getAllUserFollowRelations(String userId) {
        return Observable
                .defer(() ->
                {
                    BehaviorSubject<String> pagecontrol = BehaviorSubject.create();
                    pagecontrol.onNext("start");
                    return pagecontrol.concatMap(aKey ->
                    {
                        if (aKey != null && aKey.equals("start")) {
                            return TwitchApi
                                    .getInstance()
                                    .getStreamHelixService()
                                    .getUserFollowsById(SensitiveStorage.getClientApiKey(), userId)
                                    .doOnSuccess(page -> {
                                        if (page.getPagination() != null &&
                                                page.getPagination().getCursor() != null)
                                            pagecontrol.onNext(page.getPagination().getCursor());
                                        else pagecontrol.onComplete();
                                    })
                                    .toObservable();
                        } else if (aKey != null) {
                            return TwitchApi
                                    .getInstance()
                                    .getStreamHelixService()
                                    .getUserFollowsById(SensitiveStorage.getClientApiKey(), userId, aKey)
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
