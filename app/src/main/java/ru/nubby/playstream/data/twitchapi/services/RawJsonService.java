package ru.nubby.playstream.data.twitchapi.services;


import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Url;


public interface RawJsonService {

    @GET()
    Single<String> getRawJsonFromPath(@Url String path);

}