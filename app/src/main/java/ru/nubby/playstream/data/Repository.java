package ru.nubby.playstream.data;

import java.util.HashMap;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import ru.nubby.playstream.model.FollowRelations;
import ru.nubby.playstream.model.Pagination;
import ru.nubby.playstream.model.Quality;
import ru.nubby.playstream.model.Stream;
import ru.nubby.playstream.model.StreamsRequest;
import ru.nubby.playstream.model.UserData;

public interface Repository {
    /**
     * Gets stream list from remote or local(cached) repository
     * @return list of top streams
     */
    Single<StreamsRequest> getStreams();

    /**
     * Gets stream list from remote or local(cached) repository
     * @param pagination {@link Pagination} cursor
     * @return list of streams after pagination cursor
     */
    Single<StreamsRequest> getStreams(Pagination pagination);

    /**
     * Gets user follows list from remote or local(cached) repository
     * @param userId current (logged) user id
     * @return list of user's followed streams
     */
    Single<List<FollowRelations>> getUserFollows(String userId);

    /**
     * Synchronizes user follows list from remote to local repository
     * @param userId current (logged) user id
     * @return always true if fetch went well, error in rx style if something happened.
     */
    public Single<Boolean> synchronizeFollows(String userId);

    /**
     * Gets active stream list from remote or local(cached) repository
     * @param userId current (logged) user id
     * @return list of user's followed streams
     */
    Single<List<Stream>> getLiveStreamsFollowedByUser(String userId);

    /**
     * Gets video url from stream object
     *
     * @param stream {@link Stream}
     * @return HashMap of Qualities as keys, and Urls to hls resources as values
     */
    Single<HashMap<Quality, String>> getVideoUrl(Stream stream);

    /**
     * Gets login name in latin for further queries.
     *
     * @param stream {@link Stream} object
     * @return {@link Single} of login name string.
     */
    Single<UserData> getStreamerInfo(Stream stream);

    /**
     * Gets {@link UserData} for currently logged user.
     *
     * @param token String OAUTH2 token
     * @return {@link Single} of {@link UserData} object, related to logged user.
     */
    public Single<UserData> getUserDataFromToken(String token);

    /**
     * Updates {@link Stream} information.
     *
     * @param stream {@link Stream} object
     * @return {@link Single} of {@link Stream} with updated user counter.
     */
    public Observable<Stream> getUpdatedStreamInfo(Stream stream);
}
