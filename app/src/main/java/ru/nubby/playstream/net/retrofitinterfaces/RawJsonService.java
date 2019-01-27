package ru.nubby.playstream.net.retrofitinterfaces;


import io.reactivex.Observable;

import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Url;


public interface RawJsonService {
    @GET()
    Observable<String> rawJson(@Header("client-id") String clientId,@Url String path);
}