package ru.nubby.playstream.twitchapi.services;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;
import ru.nubby.playstream.model.StreamsRequest;
import ru.nubby.playstream.model.UserDataList;
import ru.nubby.playstream.model.UserFollowsRequest;

public interface TwitchStreamsHelixService {

    @GET("streams")
    Single<StreamsRequest> getTopStreams(@Header("client-id") String clientId);

    @GET("users")
    Single<UserDataList> getUserDataListById(@Header("client-id") String clientId, @Query("id") String streamId);

    @GET("users")
    Single<UserDataList> getUserDataListByToken(@Header("client-id") String clientId, @Header("Authorization") String token);

    @GET("users/follows?first=100")
    Single<UserFollowsRequest> getUserFollowsById(@Header("client-id") String clientId, @Query("from_id") String userId);

    @GET("users/follows?first=100")
    Single<UserFollowsRequest> getUserFollowsById(@Header("client-id") String clientId, @Query("from_id") String userId, @Query("after") String cursor);

    @GET("streams")
    Single<StreamsRequest> updateStream(@Header("client-id") String clientId, @Query("user_id") String userId);

    @GET("streams?first=100")
    Single<StreamsRequest> getAllStreamsByUserList(@Header("client-id") String clientId, @Query("user_id") List<String> userId);

    @GET("streams")
    Single<StreamsRequest> getMoreStreamsAfter(@Header("client-id") String clientId, @Query("after") String cursor);

}
