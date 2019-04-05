package ru.nubby.playstream.data;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Completable;
import io.reactivex.Single;
import ru.nubby.playstream.data.database.LocalRepository;
import ru.nubby.playstream.data.twitchapi.RemoteRepository;
import ru.nubby.playstream.domain.entities.FollowRelations;
import ru.nubby.playstream.domain.entities.Stream;
import ru.nubby.playstream.domain.entities.UserData;

@Singleton
public class UsersRepository {

    private final LocalRepository mLocalRepository;
    private final RemoteRepository mRemoteRepository;

    private boolean mFollowsFullUpdate = true;

    @Inject
    public UsersRepository(LocalRepository localRepository,
                           RemoteRepository remoteRepository) {
        mLocalRepository = localRepository;
        mRemoteRepository = remoteRepository;
    }

    /**
     * Gets user follows list from remote or local(cached) repository
     * @param userId current (logged) user id
     * @return list of user's followed streams
     */
    public Single<List<FollowRelations>> getUserFollows(String userId) {
        if (mFollowsFullUpdate) {
            return mRemoteRepository
                    .getUserFollows(userId)
                    .doOnSuccess(followRelationsList -> mFollowsFullUpdate = false)
                    .flatMap(followRelationsList ->
                            mLocalRepository
                                    .deleteAllFollowRelationsEntries()
                                    .andThen(mLocalRepository
                                            .insertFollowRelationsList(followRelationsList.toArray(
                                                    new FollowRelations[0])))
                                    .andThen(Single.just(followRelationsList)));

        } else {
            return mLocalRepository
                    .getFollowRelationsEntriesById(userId);
        }
    }

    /**
     * Gets {@link UserData} bound to that stream for further queries.
     *
     * @param stream stream object
     * @return user data object (watch link).
     */
    public Single<UserData> getUserFromStreamer(Stream stream) {
        return mLocalRepository
                .findUserDataById(stream.getUserId())
                .switchIfEmpty(mRemoteRepository
                        .getStreamerInfo(stream)
                        .flatMap(userData ->
                                mLocalRepository
                                        .insertUserData(userData)
                                        .andThen(Single.just(userData))));
    }

    /**
     * Gets {@link UserData} for currently logged user.
     *
     * @param token String OAUTH2 token
     * @return user data object, related to logged user.
     */
    public Single<UserData> getUserFromToken(String token) {
        return mRemoteRepository
                .getUserDataFromToken(token)
                .flatMap(userData ->
                        mLocalRepository
                                .insertUserData(userData)
                                .andThen(Single.just(userData)));
    }

    /**
     * Updates all userData entries in local database from remote.
     * @return Might return error in rx style if something happened.
     */
    public Completable synchronizeUserData() {
        return mLocalRepository
                .getAllUserDataEntries()
                .toSingle()
                .flatMap(mRemoteRepository::getUpdatedUserDataList)
                .flatMapCompletable(updatedUserDataList -> mLocalRepository
                        .insertUserDataList(updatedUserDataList.toArray(new UserData[0])));
    }
}
