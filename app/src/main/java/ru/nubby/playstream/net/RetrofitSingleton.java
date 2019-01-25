package ru.nubby.playstream.net;

import android.util.Log;


import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.nubby.playstream.model.GsonScheme;
import ru.nubby.playstream.model.Stream;

public class RetrofitSingleton {
    private static RetrofitSingleton mInstance;
    private static final String BASE_URL = "https://api.twitch.tv/helix/";
    private Retrofit mRetrofit;
    private ResponceListener mResponceListener;

    private RetrofitSingleton() {
        mRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static RetrofitSingleton getInstance() {
        if (mInstance == null) {
            mInstance = new RetrofitSingleton();
        }
        return mInstance;
    }

    public void getStreamsTest(ResponceListener callback) {
        String result = "";
        mResponceListener = callback;
        TwitchStreamsService service = mRetrofit
                .create(TwitchStreamsService.class);
            service.listStreams().enqueue(new Callback<GsonScheme>() {
                @Override
                public void onResponse(Call<GsonScheme> call, Response<GsonScheme> response) {
                    //TODO implement all shit in RX
                    if (response.body() != null) {
                        mResponceListener.callback(response.body().getData());
                        mResponceListener = null;
                    }
                }

                @Override
                public void onFailure(Call<GsonScheme> call, Throwable t) {
                    Log.i("RETROFIT RESP", call.toString());
                    t.printStackTrace();
                }
            });

    }
}
