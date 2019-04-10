package ru.nubby.playstream.data.sources.twitchapi.services;


import java.util.List;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Path;
import ru.nubby.playstream.domain.entities.ChannelPanel;
import ru.nubby.playstream.domain.entities.StreamToken;

public interface TwitchApiService {

    @GET("channels/{streamId}/access_token")
    Single<StreamToken> getAccessToken(@Path("streamId") String streamId);

    @GET("channels/{userId}/panels")
    Single<List<ChannelPanel>> getChannelPanelsForUser(@Path("userId") String userId);
}
