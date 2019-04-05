package ru.nubby.playstream.data.twitchapi;

import java.util.HashMap;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import ru.nubby.playstream.domain.entities.FollowRelations;
import ru.nubby.playstream.domain.entities.Pagination;
import ru.nubby.playstream.domain.entities.Quality;
import ru.nubby.playstream.domain.entities.Stream;
import ru.nubby.playstream.domain.entities.StreamsResponse;
import ru.nubby.playstream.domain.entities.UserData;


public interface RemoteRepository {

    Single<HashMap<Quality, String>> getQualityUrls(Stream stream);

    Single<UserData> getStreamerInfo(Stream stream);

    Single<List<UserData>> getUserDataListByStreamList(List<Stream> streamIdList);

    Single<List<UserData>> getUpdatedUserDataList(List<UserData> userDataList);

    Single<UserData> getUserDataFromToken(String token);

    Observable<Stream> getUpdatedStreamInfo(Stream stream);

    Single<StreamsResponse> getTopStreams();

    Single<StreamsResponse> getTopStreams(Pagination pagination);

    Single<List<FollowRelations>> getUserFollows(String userId);

    Single<List<Stream>> getLiveStreamsFollowedByUser(String userId);

    Single<List<Stream>> getLiveStreamsFromRelationList(
            Single<List<FollowRelations>> singleFollowRelationsList);

    Completable followTargetUser(String token, String userId, String targetUserId);

    Completable unfollowTargetUser(String token, String userId, String targetUserId);

}
