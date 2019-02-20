package ru.nubby.playstream.data.twitchapi.services;


import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import ru.nubby.playstream.model.StreamToken;

public interface TwitchStreamsApiService {

    @GET("channels/{streamId}/access_token")
    Single<StreamToken> getAccessToken(@Header("client-id") String clientId, @Path("streamId") String streamId);

}
