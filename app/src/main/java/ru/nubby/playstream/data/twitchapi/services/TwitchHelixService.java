package ru.nubby.playstream.data.twitchapi.services;

import java.util.List;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;
import ru.nubby.playstream.domain.entity.StreamsResponse;
import ru.nubby.playstream.domain.entity.UserDataList;
import ru.nubby.playstream.domain.entity.UserFollowsResponse;

public interface TwitchHelixService {

    @GET("streams")
    Single<StreamsResponse> getTopStreams(@Query("after") String cursor);

    @GET("users")
    Single<UserDataList> getUserDataListByIds(@Query("id") List<String> streamIdList);

    @GET("users")
    Single<UserDataList> getUserDataListByToken(@Header("Authorization") String token);

    @GET("users/follows?first=100")
    Single<UserFollowsResponse> getUserFollowsById(@Query("from_id") String userId,
                                                   @Query("after") String cursor);

    @GET("streams?first=100")
    Single<StreamsResponse> getAllStreamsByUserList(@Query("user_id") List<String> userId);

}
