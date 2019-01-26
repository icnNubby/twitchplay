package ru.nubby.playstream.net.retrofitinterfaces;


import io.reactivex.Observable;
import retrofit2.http.GET;


public interface RawJsonService {
    @GET()
    Observable<String> rawJson();
}