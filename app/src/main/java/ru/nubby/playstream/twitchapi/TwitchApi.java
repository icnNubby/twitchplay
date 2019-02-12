package ru.nubby.playstream.twitchapi;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import ru.nubby.playstream.twitchapi.services.RawJsonService;
import ru.nubby.playstream.twitchapi.services.TwitchStreamsHelixService;
import ru.nubby.playstream.twitchapi.services.TwitchStreamsApiService;

//TODO prob extract repeating path from retrofit builder to some private method, and build upon it
/**
 * Some of requests will not work with helix API, some will not work with old api API.
 * after building {@link TwitchStreamsApiService} object, you should know which endpoint to use
 */
public class TwitchApi {

    private static TwitchApi mInstance;
    private static final String BASE_URL_HELIX = "https://api.twitch.tv/helix/";
    private static final String BASE_URL_KRAKEN = "https://api.twitch.tv/kraken/";
    private static final String BASE_URL_API = "https://api.twitch.tv/api/";
    private static final String BASE_URL_USHER_HLS = "https://usher.ttvnw.net/api/channel/hls/";
    private OkHttpClient mOkHttpClient;

    private TwitchApi() {
        mOkHttpClient = provideOkHttpClient();
    }

    public static synchronized TwitchApi getInstance() {
        if (mInstance == null) {
            mInstance = new TwitchApi();
        }
        return mInstance;
    }

    public TwitchStreamsHelixService getStreamHelixService() {

        return new Retrofit.Builder()
                .baseUrl(BASE_URL_HELIX)
                .client(mOkHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(TwitchStreamsHelixService.class);

    }

    public TwitchStreamsApiService getStreamApiService() {

        return new Retrofit.Builder()
                .baseUrl(BASE_URL_API)
                .client(mOkHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(TwitchStreamsApiService.class);

    }

    public RawJsonService getRawJsonHlsService() {

        return new Retrofit.Builder()
                .baseUrl(BASE_URL_USHER_HLS)
                .client(mOkHttpClient)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(RawJsonService.class);

    }

    private OkHttpClient provideOkHttpClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new OkHttpClient().newBuilder()
                .connectTimeout(5000, TimeUnit.MILLISECONDS) //TODO think what to do incase of reaaaaaaallly bbaaaaaaadd connetion
                .readTimeout(5000, TimeUnit.MILLISECONDS)
                .addInterceptor(logging)
                .build();
    }
}
