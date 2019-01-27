package ru.nubby.playstream.net;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import ru.nubby.playstream.model.GsonScheme;
import ru.nubby.playstream.model.Stream;
import ru.nubby.playstream.SensitiveStorage;
import ru.nubby.playstream.model.Token;
import ru.nubby.playstream.net.retrofitinterfaces.RawJsonService;
import ru.nubby.playstream.net.retrofitinterfaces.TwitchStreamUrl;
import ru.nubby.playstream.net.retrofitinterfaces.TwitchStreamsService;

public class RetrofitSingleton {

    private static RetrofitSingleton mInstance;
    private static final String BASE_URL_HELIX = "https://api.twitch.tv/helix/";
    private static final String BASE_URL_API = "https://api.twitch.tv/api/";
    private static final String BASE_URL_USHER_HLS = "https://usher.ttvnw.net/api/channel/hls/";
    private Retrofit mRetrofit;
    private OkHttpClient mOkHttpClient;

    private RetrofitSingleton() {
        mOkHttpClient = provideOkHttpClient();
    }

    public static synchronized RetrofitSingleton getInstance() {
        if (mInstance == null) {
            mInstance = new RetrofitSingleton();
        }
        return mInstance;
    }

    public Observable<List<Stream>> getStreamsTest() {

        mRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL_HELIX)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        TwitchStreamsService service = mRetrofit.create(TwitchStreamsService.class);

        return service.gsonScheme(SensitiveStorage.getClientApiKey())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(GsonScheme::getData);

    }

    public Observable<Token> getStreamUrl(Stream stream) { //TODO fix name

        mRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL_API)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        TwitchStreamUrl service = mRetrofit.create(TwitchStreamUrl.class);

        return service.token(SensitiveStorage.getClientApiKey(), stream.getStreamerName().toLowerCase())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

    }

    public Observable<String> getRawJson(String url) {
        mRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL_USHER_HLS)
                .client(mOkHttpClient)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        RawJsonService service = mRetrofit.create(RawJsonService.class);

        return service.rawJson(SensitiveStorage.getClientApiKey(), url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

    }

    private OkHttpClient provideOkHttpClient()
    {
        //this is the part where you will see all the logs of retrofit requests
        //and responses
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new OkHttpClient().newBuilder()
                .connectTimeout(500, TimeUnit.MILLISECONDS)
                .readTimeout(500,TimeUnit.MILLISECONDS)
                .addInterceptor(logging)
                .build();
    }
}
