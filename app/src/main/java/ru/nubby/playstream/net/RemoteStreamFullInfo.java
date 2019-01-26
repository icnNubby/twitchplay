package ru.nubby.playstream.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Observable;
import ru.nubby.playstream.model.Stream;
import ru.nubby.playstream.model.Token;

public class RemoteStreamFullInfo {

    /**
     * Gets video url from stream object
     * @param stream Stream
     * @return url as string
     */
    public Observable<String> getVideoUrl(Stream stream) { //TODO make a raw fetch from json
        Observable<Token> tokenObservable = RetrofitSingleton.getInstance().getStreamUrl(stream);
        return  tokenObservable.map(x ->
             String.format("https://usher.ttvnw.net/api/channel/hls/%s.m3u8" +
                    "?player=twitchweb" +
                    "&token=%s" +
                    "&sig=%s" +
                    "&allow_audio_only=true" +
                    "&allow_source=true" +
                    "&type=any" +
                    "&p=%s",
                     stream.getStreamerName(),
                     x.getToken().replaceAll("\\\\",""),
                     x.getSig(),
                     "" + new Random().nextInt(6)));
    }



}
