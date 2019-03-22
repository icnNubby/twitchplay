package ru.nubby.playstream.data.twitchapi.services;


import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Url;
import ru.nubby.playstream.data.twitchapi.converter.Scalars;


public interface RawJsonService {

    @GET()
    @Scalars
    Single<String> getRawJsonFromPath(@Url String path);

}