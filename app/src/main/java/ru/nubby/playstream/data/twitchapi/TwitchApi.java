package ru.nubby.playstream.data.twitchapi;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import ru.nubby.playstream.SensitiveStorage;
import ru.nubby.playstream.data.twitchapi.services.RawJsonService;
import ru.nubby.playstream.data.twitchapi.services.TwitchHelixService;
import ru.nubby.playstream.data.twitchapi.services.TwitchApiService;
import ru.nubby.playstream.data.twitchapi.services.TwitchKrakenService;

//TODO prob extract repeating path from retrofit builder to some private method, and build upon it
/**
 * Some of requests will not work with helix API, some will not work with old api API.
 * after building {@link TwitchApiService} object, you should know which endpoint to use
 */
public class TwitchApi {

    private static TwitchApi sInstance;
    private static final String BASE_URL_HELIX = "https://api.twitch.tv/helix/";
    private static final String BASE_URL_KRAKEN = "https://api.twitch.tv/kraken/";
    private static final String BASE_URL_API = "https://api.twitch.tv/api/";
    private static final String BASE_URL_USHER_HLS = "https://usher.ttvnw.net/api/channel/hls/";

    private OkHttpClient mOkHttpClient;
    private TwitchHelixService mTwitchHelixService;
    private TwitchApiService mTwitchApiService;
    private TwitchKrakenService mTwitchKrakenService;
    private RawJsonService mRawJsonService;

    private TwitchApi() {
        mOkHttpClient = provideOkHttpClient();
    }

    public static synchronized TwitchApi getInstance() {
        if (sInstance == null) {
            sInstance = new TwitchApi();
        }
        return sInstance;
    }

    TwitchHelixService getStreamHelixService() {
        if (mTwitchHelixService == null) {
            mTwitchHelixService = new Retrofit.Builder()
                    .baseUrl(BASE_URL_HELIX)
                    .client(mOkHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build()
                    .create(TwitchHelixService.class);
        }
        return mTwitchHelixService;
    }

    TwitchApiService getStreamApiService() {
        if (mTwitchApiService == null) {
            mTwitchApiService = new Retrofit.Builder()
                    .baseUrl(BASE_URL_API)
                    .client(mOkHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build()
                    .create(TwitchApiService.class);
        }
        return mTwitchApiService;

    }

    RawJsonService getRawJsonHlsService() {
        if (mRawJsonService == null){
            mRawJsonService = new Retrofit.Builder()
                    .baseUrl(BASE_URL_USHER_HLS)
                    .client(mOkHttpClient)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build()
                    .create(RawJsonService.class);
        }
        return mRawJsonService;

    }

    TwitchKrakenService getKrakenService() {
        if (mTwitchKrakenService == null){
            mTwitchKrakenService = new Retrofit.Builder()
                    .baseUrl(BASE_URL_KRAKEN)
                    .client(mOkHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build()
                    .create(TwitchKrakenService.class);
        }
        return mTwitchKrakenService;
    }

    private OkHttpClient provideOkHttpClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        Interceptor tokenInterceptor = new RequestTokenInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new OkHttpClient().newBuilder()
                .connectTimeout(5000, TimeUnit.MILLISECONDS)
                //TODO think what to do incase of reaaaaaaallly bbaaaaaaadd connetion
                .readTimeout(5000, TimeUnit.MILLISECONDS)
                .addInterceptor(logging)
                .addInterceptor(tokenInterceptor)
                .build();
    }

    private class RequestTokenInterceptor implements Interceptor {
        @Override
        public okhttp3.Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            Request newRequest;

            newRequest = request.newBuilder()
                    .addHeader(SensitiveStorage.getHeaderClientId(), SensitiveStorage.getClientApiKey())
                    .build();
            return chain.proceed(newRequest);
        }
    }
}
