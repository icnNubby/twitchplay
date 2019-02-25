package ru.nubby.playstream.data.twitchapi.services;

import java.util.List;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;
import ru.nubby.playstream.model.StreamsRequest;
import ru.nubby.playstream.model.UserDataList;
import ru.nubby.playstream.model.UserFollowsRequest;

public interface TwitchHelixService {

    @GET("streams")
    Single<StreamsRequest> getTopStreams();

    @GET("users")
    Single<UserDataList> getUserDataListById(@Query("id") String streamId);

    @GET("users")
    Single<UserDataList> getUserDataListByIdsList(@Query("id") List<String> streamIdList);

    @GET("users")
    Single<UserDataList> getUserDataListByToken(@Header("Authorization") String token);

    @GET("users/follows?first=100")
    Single<UserFollowsRequest> getUserFollowsById(@Query("from_id") String userId);

    @GET("users/follows?first=100")
    Single<UserFollowsRequest> getUserFollowsById(@Query("from_id") String userId,
                                                  @Query("after") String cursor);

    @GET("streams")
    Single<StreamsRequest> updateStream(@Query("user_id") String userId);

    @GET("streams?first=100")
    Single<StreamsRequest> getAllStreamsByUserList(@Query("user_id") List<String> userId);

    @GET("streams")
    Single<StreamsRequest> getMoreStreamsAfter(@Query("after") String cursor);

}
