package ru.nubby.playstream.domain.twitchapi;

import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import androidx.annotation.NonNull;
import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import ru.nubby.playstream.domain.twitchapi.converter.AnnotatedConverterFactory;
import ru.nubby.playstream.domain.twitchapi.converter.Json;
import ru.nubby.playstream.domain.twitchapi.converter.Scalars;
import ru.nubby.playstream.domain.twitchapi.interceptors.HostSelectionInterceptor;
import ru.nubby.playstream.domain.twitchapi.interceptors.RequestTokenInterceptor;
import ru.nubby.playstream.domain.twitchapi.services.RawJsonService;
import ru.nubby.playstream.domain.twitchapi.services.TwitchApiService;
import ru.nubby.playstream.domain.twitchapi.services.TwitchHelixService;
import ru.nubby.playstream.domain.twitchapi.services.TwitchKrakenService;

//TODO prob extract repeating path from retrofit builder to some private method, and build upon it
//TODO should it be singleton??

/**
 * Some of requests will not work with helix API, some will not work with old api API.
 * after building {@link TwitchApiService} object, you should know which endpoint to use
 */
@Module
public class TwitchApiModule {
    private final int TIMEOUT = 5000;

    private static final String BASE_URL_HELIX = "https://api.twitch.tv/helix/";
    private static final String BASE_URL_KRAKEN = "https://api.twitch.tv/kraken/";
    private static final String BASE_URL_API = "https://api.twitch.tv/api/";
    private static final String BASE_URL_USHER_HLS = "https://usher.ttvnw.net/api/channel/hls/";

    @Provides
    @Singleton
    @NonNull
    public HttpLoggingInterceptor provideLoggingInterceptor() {
        return new HttpLoggingInterceptor();
    }

    @Provides
    @Singleton
    @NonNull
    public RequestTokenInterceptor provideTokenInterceptor() {
        return new RequestTokenInterceptor();
    }

    @Provides
    @Singleton
    @NonNull
    public HostSelectionInterceptor provideHostSelectionInterceptor() {
        return new HostSelectionInterceptor();
    }

    @Provides
    @Singleton
    @NonNull
    public OkHttpClient provideOkHttpClient(HttpLoggingInterceptor loggingInterceptor,
                                            RequestTokenInterceptor tokenInterceptor,
                                            HostSelectionInterceptor hostSelectionInterceptor) {
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new OkHttpClient().newBuilder()
                .connectTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
                .addInterceptor(loggingInterceptor)
                .addInterceptor(tokenInterceptor)
                .addInterceptor(hostSelectionInterceptor)
                .build();
    }


    @Provides
    @Singleton
    @NonNull
    public AnnotatedConverterFactory provideAnnotatedConverterFactory(){
        return new AnnotatedConverterFactory.Builder()
                .add(Scalars.class, ScalarsConverterFactory.create())
                .add(Json.class, GsonConverterFactory.create())
                .build();
    }

    @Provides
    @Singleton
    @NonNull
    public RxJava2CallAdapterFactory provideRxJava2CallAdapterFactory() {
        return RxJava2CallAdapterFactory.create();
    }


    @Provides
    @Singleton
    @NonNull
    public Retrofit provideRetrofit(OkHttpClient okHttpClient,
                                    RxJava2CallAdapterFactory callAdapterFactory,
                                    AnnotatedConverterFactory annotatedConverterFactory) {
        return new Retrofit.Builder()
                .client(okHttpClient)
                .addCallAdapterFactory(callAdapterFactory)
                .addConverterFactory(annotatedConverterFactory)
                .build();
    }

    @Provides
    @Singleton
    public TwitchHelixService provideStreamHelixService(Retrofit retrofit,
                                                    HostSelectionInterceptor interceptor) {
        interceptor.setHost(BASE_URL_HELIX);
        return retrofit
                .create(TwitchHelixService.class);
    }

    @Provides
    @Singleton
    public TwitchApiService provideStreamApiService(Retrofit retrofit,
                                                HostSelectionInterceptor interceptor) {
        interceptor.setHost(BASE_URL_API);
        return retrofit
                .create(TwitchApiService.class);

    }

    @Provides
    @Singleton
    public RawJsonService provideRawJsonHlsService(Retrofit retrofit,
                                               HostSelectionInterceptor interceptor) {
        interceptor.setHost(BASE_URL_USHER_HLS);
        return retrofit
                .create(RawJsonService.class);
    }

    @Provides
    @Singleton
    public TwitchKrakenService provideKrakenService(Retrofit retrofit,
                                                HostSelectionInterceptor interceptor) {
        interceptor.setHost(BASE_URL_KRAKEN);
        return retrofit
                .create(TwitchKrakenService.class);
    }


}
