package ru.nubby.playstream.twitchapi.services;


import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import ru.nubby.playstream.model.StreamsRequest;
import ru.nubby.playstream.model.Token;

public interface TwitchStreamsService {

    @GET("streams")
    Observable<StreamsRequest> getTopStreams(@Header("client-id") String clientId);

    @GET("channels/{streamId}/access_token")
    Observable<Token> getAccessToken(@Header("client-id") String clientId, @Path("streamId") String streamId);

}
