package ru.nubby.playstream.twitchapi;

import android.util.Log;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Observable;
import java.util.Random;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.nubby.playstream.SensitiveStorage;
import ru.nubby.playstream.model.Stream;
import ru.nubby.playstream.model.Token;
import ru.nubby.playstream.model.UserData;
import ru.nubby.playstream.utils.M3U8Parser;
import ru.nubby.playstream.utils.Quality;

public class RemoteStreamFullInfo {
    private final String TAG = "RemoteStreamFullInfo";

    /**
     * Gets video url from stream object
     *
     * @param stream Stream
     * @return url as string
     */
    public Single<HashMap<Quality, String>> getVideoUrl(Stream stream) {
        Single<String> channelName;

        if (stream.getStreamerLogin() != null && !stream.getStreamerLogin().equals("")) {
            channelName = Single
                    .just(stream.getStreamerLogin())
                    .subscribeOn(Schedulers.io());
        } else {
            channelName = getStreamerInfo(stream);
        }

        Single<Token> tokenSingle = channelName
                .flatMap(channelNameString -> TwitchApi
                        .getInstance()
                        .getStreamApiService()
                        .getAccessToken(SensitiveStorage.getClientApiKey(),
                                channelNameString.toLowerCase())
                        .subscribeOn(Schedulers.io()));

        return Single
                .zip(tokenSingle, channelName,
                        (token, streamerName) ->
                                String.format("%s.m3u8" +
                                                "?token=%s" +
                                                "&sig=%s" +
                                                "&player=twitchweb" +
                                                "&allow_audio_only=true" +
                                                "&allow_source=true" +
                                                "&type=any" +
                                                "&p=%s",
                                        streamerName,
                                        URLEncoder.encode(token.getToken(), "UTF-8")
                                                .replaceAll("%3A", ":")
                                                .replaceAll("%2C", ","),
                                        token.getSig(),
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

    public Single<String> getStreamerInfo(Stream stream) {
        return TwitchApi
                .getInstance()
                .getStreamHelixService()
                .getUserDataList(SensitiveStorage.getClientApiKey(),
                        stream.getUserId())
                .subscribeOn(Schedulers.io())
                .filter(userDataList -> !userDataList.getData().isEmpty())
                .map(userDataList -> userDataList.getData().get(0).getLogin())
                .doOnSuccess(login -> Log.d(TAG, "Login is " + login))
                .doOnError(error -> Log.e(TAG, "Error while getting streamer info " + error, error))
                .toSingle(stream.getStreamerName())
                .observeOn(AndroidSchedulers.mainThread());
    }


}
