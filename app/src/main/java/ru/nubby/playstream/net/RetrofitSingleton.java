package ru.nubby.playstream.net;

import java.util.List;

import io.reactivex.Observable;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.nubby.playstream.model.GsonScheme;
import ru.nubby.playstream.model.Stream;
import ru.nubby.playstream.sensitivekey.SensitiveStorage;

public class RetrofitSingleton {

    private static RetrofitSingleton mInstance;
    private static final String BASE_URL = "https://api.twitch.tv/helix/";
    private Retrofit mRetrofit;

    private RetrofitSingleton() {
        mRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }

    public static synchronized RetrofitSingleton getInstance() {
        if (mInstance == null) {
            mInstance = new RetrofitSingleton();
        }
        return mInstance;
    }

    public Observable<List<Stream>> getStreamsTest() {
        TwitchStreamsService service = mRetrofit.create(TwitchStreamsService.class);

        return service.gsonScheme(SensitiveStorage.getClientApiKey())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(GsonScheme::getData);

    }

    public Observable<List<Stream>> getStreamUrl() {
        TwitchStreamsService service = mRetrofit.create(TwitchStreamsService.class);

        return service.gsonScheme(SensitiveStorage.getClientApiKey())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(GsonScheme::getData);

    }

}
