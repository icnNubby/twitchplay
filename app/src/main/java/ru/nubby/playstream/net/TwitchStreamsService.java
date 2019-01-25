package ru.nubby.playstream.net;


import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import ru.nubby.playstream.model.GsonScheme;

public interface TwitchStreamsService {
    @Headers("Client-ID: yzy54ml2162y3lzwzmko2lnq7pgujt") // TODO HIDE THHIS SHIT
    @GET("streams")
    Call<GsonScheme> listStreams();
    //Call<List<Stream>> listStreams();
}
