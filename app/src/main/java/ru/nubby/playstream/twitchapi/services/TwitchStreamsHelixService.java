package ru.nubby.playstream.twitchapi.services;

import io.reactivex.Observable;
import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;
import ru.nubby.playstream.model.StreamsRequest;
import ru.nubby.playstream.model.UserDataList;

public interface TwitchStreamsHelixService {

    @GET("streams")
    Single<StreamsRequest> getTopStreams(@Header("client-id") String clientId);

    @GET("users")
    Single<UserDataList> getUserDataList(@Header("client-id") String clientId, @Query("id") String streamId);

    @GET("streams")
    Single<StreamsRequest> updateStream(@Header("client-id") String clientId, @Query("user_id") String userId);

    @GET("streams")
    Single<StreamsRequest> getMoreStreamsAfter(@Header("client-id") String clientId, @Query("after") String cursor);

}
