package ru.nubby.playstream.data.sources.twitchapi.services;

import java.util.List;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;
import ru.nubby.playstream.domain.entities.GamesResponse;
import ru.nubby.playstream.domain.entities.StreamsResponse;
import ru.nubby.playstream.domain.entities.UserDataResponse;
import ru.nubby.playstream.domain.entities.UserFollowsResponse;

public interface TwitchHelixService {

    //streams
    @GET("streams")
    Single<StreamsResponse> getTopStreams(@Query("after") String cursor);

    @GET("streams?first=100")
    Single<StreamsResponse> getAllStreamsByUserList(@Query("user_id") List<String> userIds);

    //users
    @GET("users")
    Single<UserDataResponse> getUserDataByIds(@Query("id") List<String> streamIdList);

    @GET("users")
    Single<UserDataResponse> getUserDataByToken(@Header("Authorization") String token);

    @GET("users/follows?first=100")
    Single<UserFollowsResponse> getUserFollowsById(@Query("from_id") String userId,
                                                   @Query("after") String cursor);

    //games
    @GET("games/top?first=100")
    Single<GamesResponse> getTopGames(@Query("after") String cursor);

    @GET("games?first=100")
    Single<GamesResponse> getGamesByIds(@Query("id")  List<String> gameIds);

}
