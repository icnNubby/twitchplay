package ru.nubby.playstream.data;

import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import ru.nubby.playstream.data.database.LocalDataSource;
import ru.nubby.playstream.data.twitchapi.RemoteRepository;
import ru.nubby.playstream.model.FollowRelations;
import ru.nubby.playstream.model.Pagination;
import ru.nubby.playstream.model.Quality;
import ru.nubby.playstream.model.Stream;
import ru.nubby.playstream.model.StreamsRequest;
import ru.nubby.playstream.model.UserData;
import ru.nubby.playstream.utils.SharedPreferencesManager;

/**
 * Contains decision making on what kind of repo we should use.
 * Some logic definitely can be decoupled into usecases/interactors.
 * Although its already some sort of interactor.
 */
public class GlobalRepository implements Repository {
    public enum LoggedStatus {
        NOT_LOGGED, TOKEN_ONLY, LOGGED
    }

    private final String TAG = GlobalRepository.class.getSimpleName();

    private final RemoteRepository mRemoteRepository;
    private final LocalDataSource mLocalDataSource;

    private static GlobalRepository sInstance;

    private boolean firstLoad = true; //TODO IDK implement in some other way its too hacky

    private GlobalRepository(@NonNull RemoteRepository remoteRepository, @NonNull LocalDataSource localDataSource) {
        mRemoteRepository = remoteRepository;
        mLocalDataSource = localDataSource;
    }

    public synchronized static void init(@NonNull RemoteRepository remoteRepository, @NonNull LocalDataSource localDataSource) {
        if (sInstance == null) {
            sInstance = new GlobalRepository(remoteRepository, localDataSource);
        }
    }

    @NonNull
    public static GlobalRepository getInstance() {
        if (sInstance == null)
            throw new NullPointerException("Global repository is not instantiated");
        return sInstance;
    }

    @Override
    public Single<StreamsRequest> getStreams() {
        return mRemoteRepository.getStreams();
    }

    @Override
    public Single<StreamsRequest> getStreams(Pagination pagination) {
        return mRemoteRepository.getStreams(pagination);
    }

    @Override
    public Single<List<FollowRelations>> getUserFollows(String userId) {
        if (firstLoad) {
            firstLoad = false;
            return mRemoteRepository
                    .getUserFollows(userId)
                    .subscribeOn(Schedulers.io())
                    .flatMap(followRelationsList ->
                            mLocalDataSource
                                    .insertFollowRelationsList(
                                            followRelationsList.toArray(new FollowRelations[0]))
                                    .andThen(Single.create(emitter ->
                                            emitter.onSuccess(followRelationsList))));

        } else {
            return mLocalDataSource
                    .getFollowRelationsEntriesById(userId)
                    .subscribeOn(Schedulers.io());
        }
    }

    @Override
    public Single<Boolean> synchronizeFollows(String userId) {
        return getUserFollows(userId)
                .subscribeOn(Schedulers.io())
                .flatMap(list -> {
                    mLocalDataSource.insertFollowRelationsList(list.toArray(new FollowRelations[0]));
                    return mLocalDataSource.getFollowRelationsEntriesById(userId);
                })
                .map(followRelationsList -> true);
    }

    @Override
    public Single<List<Stream>> getLiveStreamsFollowedByUser() {
        return getCurrentLoginInfo()
                .flatMap(userData ->
                        mRemoteRepository
                                .getLiveStreamsFromRelationList(getUserFollows(userData.getId())));
    }

    @Override
    public Single<HashMap<Quality, String>> getVideoUrl(Stream stream) {
        return mRemoteRepository.getVideoUrl(stream);
    }

    @Override
    public Single<UserData> getStreamerInfo(Stream stream) {
        return mRemoteRepository.getStreamerInfo(stream);
    }

    @Override
    public Single<UserData> getUserDataFromToken(String token) {
        return mRemoteRepository.getUserDataFromToken(token);
    }

    @Override
    public Observable<Stream> getUpdatedStreamInfo(Stream stream) {
        return mRemoteRepository.getUpdatedStreamInfo(stream);
    }

    @Override
    public Completable followUser(String targetUser) {
        return getCurrentLoginInfo()
                .subscribeOn(Schedulers.io())
                .flatMapCompletable(userData ->
                        mRemoteRepository
                                .followTargetUser(SharedPreferencesManager.getUserAccessToken(),
                                        userData.getId(), targetUser)
                                .andThen(mLocalDataSource.insertFollowRelationsEntry(
                                        new FollowRelations(userData.getId(), userData.getLogin(),
                                                targetUser, "", ""))));
        //todo fix empty fields;
    }

    @Override
    public Completable unfollowUser(String targetUser) {
        return getCurrentLoginInfo()
                .subscribeOn(Schedulers.io())
                .flatMapCompletable(userData ->
                        mRemoteRepository
                                .unfollowTargetUser(SharedPreferencesManager.getUserAccessToken(),
                                        userData.getId(), targetUser)
                                .andThen(mLocalDataSource.deleteFollowRelationsEntry(
                                        new FollowRelations(userData.getId(), "",
                                                targetUser, "", ""))));
    }

    @Override
    public Single<Boolean> isUserFollowed(String targetUser) {
        return getCurrentLoginInfo()
                .flatMap(userData -> mLocalDataSource.findRelation(userData.getId(), targetUser))
                .flatMap(followRelationsList ->
                        Single.create(emitter -> emitter.onSuccess(followRelationsList.isEmpty())));
    }

    @Override
    public Single<UserData> getCurrentLoginInfo() {
        LoggedStatus currentStatus = getCurrentLoggedStatus();
        if (currentStatus == LoggedStatus.NOT_LOGGED) {
            return Single.create(emitter -> emitter.onError(new Throwable("Not logged in")));
        } else if (currentStatus == LoggedStatus.LOGGED) {
            return Single.create(emitter -> {
                emitter.onSuccess(SharedPreferencesManager.getUserData());
            });
        } else { //LoggedStatus.TOKEN_ONLY
            return getUserDataFromToken(SharedPreferencesManager.getUserAccessToken())
                    .doOnSuccess(SharedPreferencesManager::setUserData);
        }
    }

    @Override
    public Single<UserData> loginAttempt(String token) {
        SharedPreferencesManager.setUserAccessToken(token);
        return getCurrentLoginInfo();
    }

    private LoggedStatus getCurrentLoggedStatus() {
        String token = SharedPreferencesManager.getUserAccessToken();
        if (token != null && !token.equals("")) {
            UserData data = SharedPreferencesManager.getUserData();
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
