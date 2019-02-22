package ru.nubby.playstream.data.twitchapi.services;

import io.reactivex.Completable;
import retrofit2.http.DELETE;
import retrofit2.http.Header;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface TwitchKrakenService {
    @PUT("/users/{user}/follows/channels/{targetUser}")
    Completable followTargetUser(@Header("Authorization") String token,
                                 @Path("user") String user,
                                 @Path("targetUser") String targetUser);

    @DELETE("/users/{user}/follows/channels/{targetUser}")
    Completable unfollowTargetUser(@Header("Authorization") String token,
                                 @Path("user") String user,
                                 @Path("targetUser") String targetUser);

}
