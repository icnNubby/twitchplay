package ru.nubby.playstream.net;

import java.util.List;

import io.reactivex.Observable;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.Url;
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
    private Retrofit mRetrofit;

    private RetrofitSingleton() {
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

    public Observable<Token> getStreamUrl(Stream stream) {

        mRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL_API)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        TwitchStreamUrl service = mRetrofit.create(TwitchStreamUrl.class);

        return service.token(SensitiveStorage.getClientApiKey(), stream.getStreamerName())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

    }

    public Observable<String> getRawJson(String url) {
        mRetrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        RawJsonService service = mRetrofit.create(RawJsonService.class);

        return service.rawJson()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

    }

}
