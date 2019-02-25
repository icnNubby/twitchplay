package ru.nubby.playstream.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import ru.nubby.playstream.data.database.LocalDataSource;
import ru.nubby.playstream.data.sharedprefs.SharedPreferencesManager;
import ru.nubby.playstream.data.twitchapi.RemoteRepository;
import ru.nubby.playstream.model.FollowRelations;
import ru.nubby.playstream.model.Pagination;
import ru.nubby.playstream.model.Quality;
import ru.nubby.playstream.model.Stream;
import ru.nubby.playstream.model.StreamsRequest;
import ru.nubby.playstream.model.UserData;

/**
 * Contains decision making on what kind of repo we should use.
 * Some logic definitely can be decoupled into usecases/interactors.
 * Although its already some sort of interactor.
 */
//todo split into some usecases(or logically connected entities ex. UsersInteractor, StreamsInteractor, etc.)
public class GlobalRepository implements Repository {
    public enum LoggedStatus {
        NOT_LOGGED, TOKEN_ONLY, LOGGED
    }

    private final String TAG = GlobalRepository.class.getSimpleName();

    private final RemoteRepository mRemoteRepository;
    private final LocalDataSource mLocalDataSource;
    private final SharedPreferencesManager mSharedPreferencesManager;

    private static GlobalRepository sInstance;

    private boolean firstLoad = true; //TODO IDK implement in some other way its too hacky

    private GlobalRepository(@NonNull RemoteRepository remoteRepository,
                             @NonNull LocalDataSource localDataSource,
                             @NonNull SharedPreferencesManager sharedPreferencesManager) {
        mRemoteRepository = remoteRepository;
        mLocalDataSource = localDataSource;
        mSharedPreferencesManager = sharedPreferencesManager;
    }

    public synchronized static void init(@NonNull RemoteRepository remoteRepository,
                                         @NonNull LocalDataSource localDataSource,
                                         @NonNull SharedPreferencesManager sharedPreferencesManager) {
        if (sInstance == null) {
            sInstance = new GlobalRepository(remoteRepository, localDataSource,
                    sharedPreferencesManager);
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
                                    .deleteAllFollowRelationsEntries()
                                    .andThen(mLocalDataSource
                                            .insertFollowRelationsList(followRelationsList.toArray(
                                                    new FollowRelations[0])))
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
                .flatMap(userData -> mRemoteRepository
                                .getLiveStreamsFromRelationList(getUserFollows(userData.getId())));
    }

    @Override
    public Single<HashMap<Quality, String>> getVideoUrl(Stream stream) {
        return mRemoteRepository.getVideoUrl(stream);
    }

    @Override
    public Single<UserData> getStreamerInfo(Stream stream) {
        return mLocalDataSource
                .findUserDataById(stream.getUserId())
                .switchIfEmpty(mRemoteRepository
                        .getStreamerInfo(stream)
                        .flatMap(userData ->
                                mLocalDataSource
                                        .insertUserData(userData)
                                        .andThen(Single.just(userData))));
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
    public Completable followStream(Stream targetStream) {
        return getCurrentLoginInfo()
                .subscribeOn(Schedulers.io())
                .flatMapCompletable(userData ->
                        mRemoteRepository
                                .followTargetUser(mSharedPreferencesManager.getUserAccessToken(),
                                        userData.getId(), targetStream.getUserId())
                                .andThen(mLocalDataSource
                                        .insertFollowRelationsEntry(
                                                new FollowRelations(userData.getId(),
                                                        userData.getLogin(),
                                                        targetStream.getUserId(),
                                                        targetStream.getStreamerLogin(),
                                                        ""))
                                        .subscribeOn(Schedulers.io())));
        //todo fix empty fields;
    }

    @Override
    public Completable unfollowStream(Stream targetStream) {
        return getCurrentLoginInfo()
                .subscribeOn(Schedulers.io())
                .flatMapCompletable(userData ->
                        mRemoteRepository
                                .unfollowTargetUser(mSharedPreferencesManager.getUserAccessToken(),
                                        userData.getId(), targetStream.getUserId())
                                .andThen(mLocalDataSource
                                        .deleteFollowRelationsEntry(
                                                new FollowRelations(userData.getId(),
                                                        userData.getLogin(),
                                                        targetStream.getUserId(),
                                                        targetStream.getStreamerLogin(), ""))
                                        .subscribeOn(Schedulers.io())));
    }

    @Override
    public Single<Boolean> isStreamFollowed(Stream targetStream) {
        return getCurrentLoginInfo()
                .flatMap(userData -> mLocalDataSource.findRelation(userData.getId(),
                        targetStream.getUserId()))
                .flatMap(followRelationsList ->
                        Single.create(emitter -> emitter.onSuccess(!followRelationsList.isEmpty())));
    }

    @Override
    public Single<UserData> getCurrentLoginInfo() {
        LoggedStatus currentStatus = getCurrentLoggedStatus();
        if (currentStatus == LoggedStatus.NOT_LOGGED) {
            return Single.create(emitter -> emitter.onError(new Throwable("Not logged in")));
        } else if (currentStatus == LoggedStatus.LOGGED) {
            return Single.create(emitter -> {
                emitter.onSuccess(mSharedPreferencesManager.getUserData());
            });
        } else { //LoggedStatus.TOKEN_ONLY
            return getUserDataFromToken(mSharedPreferencesManager.getUserAccessToken())
                    .doOnSuccess(mSharedPreferencesManager::setUserData);
        }
    }

    @Override
    public Single<UserData> loginAttempt(String token) {
        mSharedPreferencesManager.setUserAccessToken(token);
        return getCurrentLoginInfo();
    }

    private LoggedStatus getCurrentLoggedStatus() {
        String token = mSharedPreferencesManager.getUserAccessToken();
        if (token != null && !token.equals("")) {
            UserData data = mSharedPreferencesManager.getUserData();
            if (data == null) {
                return LoggedStatus.TOKEN_ONLY;
            } else {
                return LoggedStatus.LOGGED;
            }
        } else {
            return LoggedStatus.NOT_LOGGED;
        }
    }

    /*
    private Single<StreamsRequest> updateStreamsInfo(Single<StreamsRequest> streamsRequest) {
        return streamsRequest
                .map(StreamsRequest::getData)
                .//todo continue here.
    }
    */

}
