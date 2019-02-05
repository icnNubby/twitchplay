package ru.nubby.playstream.twitchapi.services;


import io.reactivex.Observable;
import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;
import ru.nubby.playstream.model.StreamsRequest;
import ru.nubby.playstream.model.Token;
import ru.nubby.playstream.model.UserData;
import ru.nubby.playstream.model.UserDataList;

public interface TwitchStreamsApiService {

    @GET("channels/{streamId}/access_token")
    Single<Token> getAccessToken(@Header("client-id") String clientId, @Path("streamId") String streamId);

}
