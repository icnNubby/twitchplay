package ru.nubby.playstream.data.twitchapi;

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
import ru.nubby.playstream.SensitiveStorage;
import ru.nubby.playstream.data.twitchapi.converter.AnnotatedConverterFactory;
import ru.nubby.playstream.data.twitchapi.converter.Json;
import ru.nubby.playstream.data.twitchapi.converter.Scalars;
import ru.nubby.playstream.data.twitchapi.interceptors.RequestTokenInterceptor;
import ru.nubby.playstream.data.twitchapi.services.RawJsonService;
import ru.nubby.playstream.data.twitchapi.services.TwitchApiService;
import ru.nubby.playstream.data.twitchapi.services.TwitchHelixService;
import ru.nubby.playstream.data.twitchapi.services.TwitchKrakenService;

/**
 * Some of requests will not work with helix API, some will not work with old api API.
 * after building {@link TwitchApiService} object, you should know which endpoint to use
 */
@Module
public class TwitchApiModule {
    private final int TIMEOUT = 5000; //todo might be given outside

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
        return new RequestTokenInterceptor(SensitiveStorage.getHeaderClientId(),
                SensitiveStorage.getClientApiKey());
    }

    @Provides
    @Singleton
    @NonNull
    public OkHttpClient provideOkHttpClient(HttpLoggingInterceptor loggingInterceptor,
                                            RequestTokenInterceptor tokenInterceptor) {
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new OkHttpClient().newBuilder()
                .connectTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
                .addInterceptor(loggingInterceptor)
                .addInterceptor(tokenInterceptor)
                .build();
    }


    @Provides
    @Singleton
    @NonNull
    public AnnotatedConverterFactory provideAnnotatedConverterFactory() {
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
    public Retrofit.Builder provideRetrofitBuilder(OkHttpClient okHttpClient,
                                                   RxJava2CallAdapterFactory callAdapterFactory,
                                                   AnnotatedConverterFactory annotatedConverterFactory) {
        return new Retrofit.Builder()
                .client(okHttpClient)
                .addCallAdapterFactory(callAdapterFactory)
                .addConverterFactory(annotatedConverterFactory);
    }

    @Provides
    @Singleton
    public TwitchHelixService provideStreamHelixService(Retrofit.Builder retrofitBuilder) {
        return retrofitBuilder
                .baseUrl(BASE_URL_HELIX)
                .build()
                .create(TwitchHelixService.class);
    }

    @Provides
    @Singleton
    public TwitchApiService provideStreamApiService(Retrofit.Builder retrofitBuilder) {
        return retrofitBuilder
                .baseUrl(BASE_URL_API)
                .build()
                .create(TwitchApiService.class);

    }

    @Provides
    @Singleton
    public RawJsonService provideRawJsonHlsService(Retrofit.Builder retrofitBuilder) {
        return retrofitBuilder
                .baseUrl(BASE_URL_USHER_HLS)
                .build()
                .create(RawJsonService.class);
    }

    @Provides
    @Singleton
    public TwitchKrakenService provideKrakenService(Retrofit.Builder retrofitBuilder) {
        return retrofitBuilder
                .baseUrl(BASE_URL_KRAKEN)
                .build()
                .create(TwitchKrakenService.class);
    }

}
