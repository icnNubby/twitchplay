package ru.nubby.playstream.domain.interactor;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import ru.nubby.playstream.data.database.LocalRepository;
import ru.nubby.playstream.data.sharedprefs.AuthorizationStorage;
import ru.nubby.playstream.data.twitchapi.RemoteRepository;
import ru.nubby.playstream.domain.entity.UserData;

/**
 * Business logic for authorization.
 * Since we fetch user data from server, we wrap results with Rx extensions.
 * <p>
 * We might have 3 logged states.
 * 1. Not logged. No token saved, no user data saved.
 * 2. Partially logged (token only). Token is saved, but no user data.
 * 3. Fully logged. Token is saved, user data is saved.
 * We duplicate user data to local repository and to auth storage.
 * <b>Only source of truth is auth storage.</b>
 * Data in local storage is not used for auth purposes, and is saved here just to prevent useless
 * duplicate requests.
 */
@Singleton
public class AuthInteractor {

    public enum LoggedStatus {
        NOT_LOGGED, TOKEN_ONLY, LOGGED
    }

    private final AuthorizationStorage mAuthorizationStorage;
    private final RemoteRepository mRemoteRepository;
    private final LocalRepository mLocalRepository;

    @Inject
    public AuthInteractor(AuthorizationStorage authorizationStorage,
                          RemoteRepository remoteRepository,
                          LocalRepository localRepository) {
        mAuthorizationStorage = authorizationStorage;
        mRemoteRepository = remoteRepository;
        mLocalRepository = localRepository;
    }

    /**
     * Gets current login info. If current login procedure was not complete
     * (status {@link LoggedStatus#TOKEN_ONLY}), then finishes it.
     *
     * @return {@link UserData} object with current logged user info, or throws exception,
     * if user is not logged in.
     */
    public Single<UserData> getCurrentLoginInfo() {
        LoggedStatus currentStatus = getCurrentLoggedStatus();
        if (currentStatus == LoggedStatus.NOT_LOGGED) {
            return Single.create(emitter -> {
                emitter.onError(new Throwable("Not logged in"));
            });
        } else if (currentStatus == LoggedStatus.LOGGED) {
            return Single.create(emitter -> {
                emitter.onSuccess(mAuthorizationStorage.getUserData());
            });
        } else { //LoggedStatus.TOKEN_ONLY
            return getUserFromToken(mAuthorizationStorage.getUserAccessToken())
                    .doOnSuccess(mAuthorizationStorage::setUserData);
        }
    }

    /**
     * Performs login attempt with given token.
     * Saves token to authorization storage, then fetches user data.
     *
     * @param token string OAuth token.
     * @return {@code UserData}, related to provided token.
     */
    public Single<UserData> loginAttempt(String token) {
        mAuthorizationStorage.setUserAccessToken(token);
        return getUserFromToken(token)
                .doOnSuccess(mAuthorizationStorage::setUserData);
    }

    /**
     * Fetches remote {@link UserData} for currently logged user.
     * Saves fetched  {@link UserData} to local storage.
     *
     * @param token String OAUTH2 token
     * @return {@code UserData}, related to logged user.
     */
    private Single<UserData> getUserFromToken(String token) {
        return mRemoteRepository
                .getUserDataFromToken(token)
                .flatMap(userData ->
                        mLocalRepository
                                .insertUserData(userData)
                                .andThen(Single.just(userData)));
    }

    /**
     * Helper function to get current status of logging procedure.
     * We may be unlogged, logged and might have only token.
     *
     * @return {@link LoggedStatus} status.
     */
    private LoggedStatus getCurrentLoggedStatus() {
        String token = mAuthorizationStorage.getUserAccessToken();
        if (token != null && !token.isEmpty()) {
            UserData data = mAuthorizationStorage.getUserData();
            if (data == null) {
                return LoggedStatus.TOKEN_ONLY;
            } else {
                return LoggedStatus.LOGGED;
            }
        } else {
            return LoggedStatus.NOT_LOGGED;
        }
    }

}
