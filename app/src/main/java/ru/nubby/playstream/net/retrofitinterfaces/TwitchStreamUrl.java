package ru.nubby.playstream.net.retrofitinterfaces;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import ru.nubby.playstream.model.Token;


public interface TwitchStreamUrl {
    @GET("channels/{streamId}/access_token")
    Observable<Token> token(@Header("Client-ID") String clientId, @Path("streamId") String streamId);
}

