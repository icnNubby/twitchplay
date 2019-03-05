package ru.nubby.playstream.domain.twitchapi.services;


import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Url;
import ru.nubby.playstream.domain.twitchapi.converter.Scalars;


public interface RawJsonService {

    @GET()
    @Scalars
    Single<String> getRawJsonFromPath(@Url String path);

}