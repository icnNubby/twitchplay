package ru.nubby.playstream.domain;

import java.util.HashMap;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import ru.nubby.playstream.domain.sharedprefs.DefaultPreferences;
import ru.nubby.playstream.model.FollowRelations;
import ru.nubby.playstream.model.Pagination;
import ru.nubby.playstream.model.Quality;
import ru.nubby.playstream.model.Stream;
import ru.nubby.playstream.model.StreamsRequest;
import ru.nubby.playstream.model.UserData;

public interface Repository {
    /**
     * Gets stream list from remote repository
     * @return list of top streams
     */
    Single<StreamsRequest> getTopStreams();

    /**
     * Gets stream list from remote repository
     * @param pagination {@link Pagination} cursor
     * @return list of streams after pagination cursor
     */
    Single<StreamsRequest> getTopStreams(Pagination pagination);

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
    Single<Boolean> synchronizeFollows(String userId);

    /**
     * Gets active stream list from remote or local(cached) repository
     * @return list of user's followed streams
     */
    Single<List<Stream>> getLiveStreamsFollowedByUser();

    /**
     * Gets video url from stream object
     *
     * @param stream {@link Stream}
     * @return HashMap of Qualities as keys, and Urls to hls resources as values
     */
    Single<HashMap<Quality, String>> getQualityUrls(Stream stream);

    /**
     * Gets {@link UserData} corresponding to that stream for further queries.
     *
     * @param stream {@link Stream} object
     * @return {@link Single} of login name string.
     */
    Single<UserData> getUserFromStreamer(Stream stream);


    /**
     * Gets {@link UserData} for currently logged user.
     *
     * @param token String OAUTH2 token
     * @return {@link Single} of {@link UserData} object, related to logged user.
     */
    Single<UserData> getUserFromToken(String token);

    /**
     * Updates {@link Stream} information.
     *
     * @param stream {@link Stream} object
     * @return {@link Single} of {@link Stream} with updated user counter.
     */
    Observable<Stream> getUpdatableStreamInfo(Stream stream);

    /**
     * Makes a request to follow targetUser by its ID.
     *
     * @param targetStream {@link Stream} target user's id.
     * @return {@link Completable} when succeeded or error.
     */
    Completable followStream(Stream targetStream);

    /**
     * Makes a request to unfollow targetStream by its ID.
     *
     * @param targetStream {@link Stream} target user's id.
     * @return {@link Completable} when succeeded or error.
     */
    Completable unfollowStream(Stream targetStream);

    /**
     * Makes request to db and returns true if logged user follows targetStream.
     * False if not.
     * @param targetStream {@link Stream} stream, relation to whom is checked
     * @return {@link Single} Boolean value of follow existence
     */
    Single<Boolean> isStreamFollowed(Stream targetStream);

    /**
     * Gets current login info. If current login procedure was not complete - finishes it.
     * @return {@link Single} of {@link UserData} object with logged user info.
     */
    Single<UserData> getCurrentLoginInfo();

    /**
     * Saves token to shared prefs and performs further login.
     * @param token {@link String} token
     * @return {@link Single} of {@link UserData} object with logged user info.
     */
    Single<UserData> loginAttempt(String token);

    /**
     * Get shared preferences of the app.
     * @return {@link DefaultPreferences} object to read shared preferences
     */
    DefaultPreferences getSharedPreferences();

}
