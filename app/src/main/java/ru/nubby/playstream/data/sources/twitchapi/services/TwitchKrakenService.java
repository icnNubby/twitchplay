package ru.nubby.playstream.data.sources.twitchapi.services;

import io.reactivex.Completable;
import io.reactivex.Single;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import ru.nubby.playstream.domain.entities.ChannelInfoV5;

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

    @Headers({"Accept: application/vnd.twitchtv.v5+json"})
    @GET("channels/{channelId}")
    Single<ChannelInfoV5> getChannelInfo(@Path("channelId") String channelId);

}
