package ru.nubby.playstream.data;

import java.util.HashMap;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import ru.nubby.playstream.data.sharedprefs.DefaultPreferences;
import ru.nubby.playstream.model.FollowRelations;
import ru.nubby.playstream.model.Pagination;
import ru.nubby.playstream.model.Quality;
import ru.nubby.playstream.model.Stream;
import ru.nubby.playstream.model.StreamListNavigationState;
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
     * @param pagination pagination cursor
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
     * Gets active stream list from remote or local(cached) repository
     * @return list of user's followed streams
     */
    Single<List<Stream>> getLiveStreamsFollowedByUser();

    /**
     * Gets video url from stream object
     *
     * @param stream stream object
     * @return HashMap of Qualities as keys, and Urls to hls resources as values
     */
    Single<HashMap<Quality, String>> getQualityUrls(Stream stream);

    /**
     * Gets {@link UserData} bound to that stream for further queries.
     *
     * @param stream stream object
     * @return user data object (watch link).
     */
    Single<UserData> getUserFromStreamer(Stream stream);

    /**
     * Gets {@link UserData} for currently logged user.
     *
     * @param token String OAUTH2 token
     * @return user data object, related to logged user.
     */
    Single<UserData> getUserFromToken(String token);

    /**
     * Gets new {@link Stream} information, such as user counter, state, etc.
     * Does not update initial object, returns new and updated one.
     *
     * @param stream Stream object
     * @return new Stream object.
     */
    Observable<Stream> getUpdatableStreamInfo(Stream stream);

    /**
     * Makes a request to follow targetUser by its ID.
     * <br>Performs next actions: <br>
     * 1. Put request to remote api. <br>
     * 2. Add follow relation to local db.
     *
     * @param targetStream {@link Stream} target user's id.
     * @return {@link Completable} when succeeded or error.
     */
    Completable followStream(Stream targetStream);

    /**
     * Makes a request to unfollow targetStream by its ID.
     * <br>Performs next actions: <br>
     * 1. Delete request to remote api. <br>
     * 2. Delete follow relation in local db.
     *
     * @param targetStream {@link Stream} target user's id.
     * @return {@link Completable} when succeeded or error.
     */
    Completable unfollowStream(Stream targetStream);

    /**
     * Makes request to db and returns true if logged user follows targetStream.
     * False if not.
     * @param targetStream {@link Stream} stream, relation to whom is checked
     * @return Boolean value of follow existence
     */
    Single<Boolean> isStreamFollowed(Stream targetStream);

    /**
     * Gets current login info. If current login procedure was not complete - finishes it.
     * @return {@link UserData} object with logged user info.
     */
    Single<UserData> getCurrentLoginInfo();

    /**
     * Saves token to shared prefs and performs further login.
     * @param token OAuth token
     * @return {@link UserData} object with logged user info.
     */
    Single<UserData> loginAttempt(String token);

    /**
     * Gets shared preferences object of the app.
     * @return {@link DefaultPreferences} object to read shared preferences
     */
    DefaultPreferences getSharedPreferences();

    /**
     * Gets navigation state of stream list screen.
     * It lives here because one presenter should emit it's changes, but other should respond
     * with view changes. We make this connection by creating Observable source.
     * @return {@link StreamListNavigationState} enum.
     */
    Observable<StreamListNavigationState> getObservableNavigationState();

    /**
     * Gets navigation state of stream list screen. Will return default value on first retrieve.
     * @return {@link StreamListNavigationState} enum.
     */
    StreamListNavigationState getCurrentNavigationState();

    /**
     * Makes getObservableNavigationState Observable to emit next state value.
     * @param state state to be emitted.
     */
    void setCurrentNavigationState(StreamListNavigationState state);

    /**
     * Synchronizes user follows list from remote to local repository.
     * @param userId current (logged) user id
     * @return Might return error in rx style if something happened.
     */
    Completable synchronizeFollows(String userId);

    /**
     * Updates all userData entries in local database from remote.
     * @return Might return error in rx style if something happened.
     */
    Completable synchronizeUserData();

    /**
     * Gets last retrieved stream list.
     * This stream list is saved in {@link ru.nubby.playstream.data.sharedprefs.PersistentStorage}
     * each time {@link #getLiveStreamsFollowedByUser()} method is called
     */
    List<Stream> getLastStreamList();
}
