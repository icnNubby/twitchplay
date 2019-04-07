package ru.nubby.playstream.domain.interactors;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Completable;
import io.reactivex.Single;
import ru.nubby.playstream.domain.UsersRepository;
import ru.nubby.playstream.domain.entities.Stream;
import ru.nubby.playstream.domain.entities.UserData;

/**
 * Business logic with users.
 */
@Singleton
public class UsersInteractor {
    private final UsersRepository mUsersRepository;

    @Inject
    public UsersInteractor(UsersRepository usersRepository) {
        mUsersRepository = usersRepository;
    }

    /**
     * Gets {@link UserData} bound to that stream for further queries.
     *
     * @param stream stream object
     * @return user data object (watch link).
     */
    public Single<UserData> getUserFromStreamer(Stream stream){
        return mUsersRepository.getUserFromStreamer(stream);
    }

    /**
     * Gets {@link UserData} for currently logged user.
     *
     * @param token String OAUTH2 token
     * @return user data object, related to logged user.
     */
    public Single<UserData> getUserFromToken(String token) {
        return mUsersRepository.getUserFromToken(token);
    }

    /**
     * Updates all userData entries in local database from remote.
     * @return Might return error in rx style if something happened.
     */
    public Completable synchronizeUserData() {
        return mUsersRepository.synchronizeUserData();
    }

}
