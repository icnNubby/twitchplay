package ru.nubby.playstream.net;

import android.util.Log;

import java.net.URLEncoder;
import java.util.Random;

import io.reactivex.Observable;
import ru.nubby.playstream.model.Stream;
import ru.nubby.playstream.model.Token;

public class RemoteStreamFullInfo {

    /**
     * Gets video url from stream object
     *
     * @param stream Stream
     * @return url as string
     */
    public Observable<String> getVideoUrl(Stream stream) { //TODO make a raw fetch from json
        Observable<Token> tokenObservable = RetrofitSingleton.getInstance().getStreamUrl(stream);
        return tokenObservable
                .doOnNext(x -> Log.d("RemoteStreamFullInfo", x.getToken()))
                .map(x -> String.format("%s.m3u8" +
                                "?token=%s" +
                                "&sig=%s" +
                                "&player=twitchweb" +
                                "&allow_audio_only=true" +
                                "&allow_source=true" +
                                "&type=any" +
                                "&p=%s",
                        stream.getStreamerName().toLowerCase(),
                        URLEncoder.encode(x.getToken(),"UTF-8")
                                .replaceAll("%3A",":")
                                .replaceAll("%2C",","),
                        x.getSig(),
                        "" + new Random().nextInt(6)))
                .doOnNext(x -> Log.d("RemoteStreamFullInfo", "https://usher.ttvnw.net/api/channel/hls/" + x))
                .flatMap(x -> RetrofitSingleton.getInstance().getRawJson(x));
    }


}
