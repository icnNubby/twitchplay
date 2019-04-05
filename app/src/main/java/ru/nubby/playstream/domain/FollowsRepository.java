package ru.nubby.playstream.domain;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;
import ru.nubby.playstream.domain.entities.FollowRelations;
import ru.nubby.playstream.domain.entities.Stream;

public interface FollowsRepository {
    /**
     * Gets user follows list from remote or local(cached) repository
     * @param userId current (logged) user id
     * @return list of user's followed streams
     */
    Single<List<FollowRelations>> getUserFollows(String userId);

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
     * Synchronizes user follows list from remote to local repository.
     * @param userId current (logged) user id
     * @return Might return error in rx style if something happened.
     */
    Completable synchronizeFollows(String userId);
}
