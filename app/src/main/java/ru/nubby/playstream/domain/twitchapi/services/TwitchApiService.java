package ru.nubby.playstream.domain.twitchapi.services;


import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import ru.nubby.playstream.model.StreamToken;

public interface TwitchApiService {

    @GET("channels/{streamId}/access_token")
    Single<StreamToken> getAccessToken(@Path("streamId") String streamId);

}
