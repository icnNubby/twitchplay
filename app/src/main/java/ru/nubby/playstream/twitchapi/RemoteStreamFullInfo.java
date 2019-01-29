package ru.nubby.playstream.twitchapi;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Random;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.nubby.playstream.SensitiveStorage;
import ru.nubby.playstream.model.Stream;
import ru.nubby.playstream.utils.M3U8Parser;
import ru.nubby.playstream.utils.Quality;

public class RemoteStreamFullInfo {

    /**
     * Gets video url from stream object
     *
     * @param stream Stream
     * @return url as string
     */
    public Observable<HashMap<Quality, String>> getVideoUrl(Stream stream) {

        return TwitchApi
                .getInstance()
                .getStreamServiceApi()
                .getAccessToken(SensitiveStorage.getClientApiKey(),
                        stream.getStreamerName().toLowerCase())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(token -> String.format("%s.m3u8" +
                                "?token=%s" +
                                "&sig=%s" +
                                "&player=twitchweb" +
                                "&allow_audio_only=true" +
                                "&allow_source=true" +
                                "&type=any" +
                                "&p=%s",
                        stream.getStreamerName().toLowerCase(),
                        URLEncoder.encode(token.getToken(),"UTF-8")
                                .replaceAll("%3A",":")
                                .replaceAll("%2C",","),
                        token.getSig(),
                        "" + new Random().nextInt(6)))
                .flatMap(urlToGetStreamPlaylist -> TwitchApi
                        .getInstance()
                        .getRawJsonService()
                        .getRawJsonFromPath(SensitiveStorage.getClientApiKey(), urlToGetStreamPlaylist)
                        .map(playlist -> M3U8Parser.parseTwitchApiResponse(playlist))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()));
    }


}
