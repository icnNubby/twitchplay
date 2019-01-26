package ru.nubby.playstream.net.retrofitinterfaces;


import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Header;
import ru.nubby.playstream.model.GsonScheme;

public interface TwitchStreamsService {
    @GET("streams")
    Observable<GsonScheme> gsonScheme(@Header("Client-ID") String clientId);
}
