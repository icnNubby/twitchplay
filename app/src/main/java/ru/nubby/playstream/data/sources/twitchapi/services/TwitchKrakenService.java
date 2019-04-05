package ru.nubby.playstream.data.sources.twitchapi.services;

import io.reactivex.Completable;
import retrofit2.http.DELETE;
import retrofit2.http.Headers;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TwitchKrakenService {

    @Headers({"Accept: application/vnd.twitchtv.v5+json"})
    @PUT("users/{user}/follows/channels/{targetUser}")
    Completable followTargetUser(@Path("user") String user,
                                 @Path("targetUser") String targetUser,
                                 @Query("oauth_token") String oauthToken);

    @Headers({"Accept: application/vnd.twitchtv.v5+json"})
    @DELETE("users/{user}/follows/channels/{targetUser}")
    Completable unfollowTargetUser(@Path("user") String user,
                                   @Path("targetUser") String targetUser,
                                   @Query("oauth_token") String oauthToken);

}
