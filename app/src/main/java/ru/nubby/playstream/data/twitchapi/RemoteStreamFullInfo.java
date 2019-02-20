package ru.nubby.playstream.data.twitchapi;

import android.util.Log;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.nubby.playstream.SensitiveStorage;
import ru.nubby.playstream.model.Stream;
import ru.nubby.playstream.model.StreamToken;
import ru.nubby.playstream.model.UserData;
import ru.nubby.playstream.utils.M3U8Parser;
import ru.nubby.playstream.model.Quality;

@Deprecated
public class RemoteStreamFullInfo {
    private final String TAG = "RemoteStreamFullInfo";

    /**
     * Gets video url from stream object
     *
     * @param stream {@link Stream}
     * @return HashMap of Qualities as keys, and Urls to hls resources as values
     */
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

    /**
     * Gets login name in latin for further queries.
     *
     * @param stream {@link Stream} object
     * @return {@link Single} of login name string.
     */
    private Single<UserData> getStreamerInfo(Stream stream) {
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

    /**
     * Gets {@link UserData} for currently logged user.
     *
     * @param token String OAUTH2 token
     * @return {@link Single} of {@link UserData} object, related to logged user.
     */
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

    /**
     * Updates {@link Stream} information.
     *
     * @param stream {@link Stream} object
     * @return {@link Single} of {@link Stream} with updated user counter.
     */
    public Observable<Stream> updateStream(Stream stream) {
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

}
