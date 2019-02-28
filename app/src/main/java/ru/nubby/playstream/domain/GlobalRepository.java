package ru.nubby.playstream.domain;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.nubby.playstream.domain.database.LocalRepository;
import ru.nubby.playstream.domain.sharedprefs.AuthorizationStorage;
import ru.nubby.playstream.domain.sharedprefs.DefaultPreferences;
import ru.nubby.playstream.domain.twitchapi.RemoteRepository;
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
    private final LocalRepository mLocalRepository;
    private final AuthorizationStorage mAuthorizationStorage;
    private final DefaultPreferences mDefaultPreferences;

    private static GlobalRepository sInstance;

    private boolean firstLoad = true; //TODO IDK implement in some other way its too hacky

    private GlobalRepository(@NonNull RemoteRepository remoteRepository,
                             @NonNull LocalRepository localRepository,
                             @NonNull AuthorizationStorage authorizationStorage,
                             @NonNull DefaultPreferences defaultPreferences) {
        mRemoteRepository = remoteRepository;
        mLocalRepository = localRepository;
        mAuthorizationStorage = authorizationStorage;
        mDefaultPreferences = defaultPreferences;
    }

    public synchronized static void init(@NonNull RemoteRepository remoteRepository,
                                         @NonNull LocalRepository localRepository,
                                         @NonNull AuthorizationStorage authorizationStorage,
                                         @NonNull DefaultPreferences defaultPreferences) {
        if (sInstance == null) {
            sInstance = new GlobalRepository(remoteRepository, localRepository,
                    authorizationStorage, defaultPreferences);
        }
    }

    @NonNull
    public static GlobalRepository getInstance() {
        if (sInstance == null) {
            throw new NullPointerException("Global repository is not instantiated");
        }
        return sInstance;
    }

    @Override
    public Single<StreamsRequest> getTopStreams() {
        return mRemoteRepository
                .getTopStreams()
                .flatMap(this::fetchAdditionalInfo)
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Single<StreamsRequest> getTopStreams(Pagination pagination) {
        return mRemoteRepository
                .getTopStreams(pagination)
                .flatMap(this::fetchAdditionalInfo)
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Single<List<FollowRelations>> getUserFollows(String userId) {
        if (firstLoad) {
            firstLoad = false;
            return mRemoteRepository
                    .getUserFollows(userId)
                    .subscribeOn(Schedulers.io())
                    .flatMap(followRelationsList ->
                            mLocalRepository
                                    .deleteAllFollowRelationsEntries()
                                    .andThen(mLocalRepository
                                            .insertFollowRelationsList(followRelationsList.toArray(
                                                    new FollowRelations[0])))
                                    .andThen(Single.create(emitter ->
                                            emitter.onSuccess(followRelationsList))));

        } else {
            return mLocalRepository
                    .getFollowRelationsEntriesById(userId)
                    .subscribeOn(Schedulers.io());
        }
    }

    @Override
    public Single<Boolean> synchronizeFollows(String userId) {
        return getUserFollows(userId)
                .subscribeOn(Schedulers.io())
                .flatMap(list -> {
                    mLocalRepository.insertFollowRelationsList(list.toArray(new FollowRelations[0]));
                    return mLocalRepository.getFollowRelationsEntriesById(userId);
                })
                .map(followRelationsList -> true);
    }

    @Override
    public Single<List<Stream>> getLiveStreamsFollowedByUser() {
        return getCurrentLoginInfo()
                .subscribeOn(Schedulers.io())
                .flatMap(userData -> mRemoteRepository
                        .getLiveStreamsFromRelationList(getUserFollows(userData.getId())))
                .flatMap(streams -> this.fetchAdditionalInfo(streams, false))
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Single<HashMap<Quality, String>> getQualityUrls(Stream stream) {
        return mRemoteRepository
                .getQualityUrls(stream)
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Single<UserData> getUserFromStreamer(Stream stream) {
        return mLocalRepository
                .findUserDataById(stream.getUserId())
                .subscribeOn(Schedulers.io())
                .switchIfEmpty(mRemoteRepository
                        .getStreamerInfo(stream)
                        .flatMap(userData ->
                                mLocalRepository
                                        .insertUserData(userData)
                                        .andThen(Single.just(userData))))
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Single<UserData> getUserFromToken(String token) {
        return mRemoteRepository
                .getUserDataFromToken(token)
                .subscribeOn(Schedulers.io())
                .flatMap(userData ->
                        mLocalRepository
                                .insertUserData(userData)
                                .andThen(Single.just(userData)))
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<Stream> getUpdatableStreamInfo(Stream stream) {
        return mRemoteRepository
                .getUpdatedStreamInfo(stream)
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Completable followStream(Stream targetStream) {
        return getCurrentLoginInfo()
                .subscribeOn(Schedulers.io())
                .flatMapCompletable(userData ->
                        mRemoteRepository
                                .followTargetUser(mAuthorizationStorage.getUserAccessToken(),
                                        userData.getId(), targetStream.getUserId())
                                .andThen(mLocalRepository
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
                                .unfollowTargetUser(mAuthorizationStorage.getUserAccessToken(),
                                        userData.getId(), targetStream.getUserId())
                                .andThen(mLocalRepository
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
                .flatMap(userData -> mLocalRepository.findRelation(userData.getId(),
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
                emitter.onSuccess(mAuthorizationStorage.getUserData());
            });
        } else { //LoggedStatus.TOKEN_ONLY
            return getUserFromToken(mAuthorizationStorage.getUserAccessToken())
                    .doOnSuccess(mAuthorizationStorage::setUserData);
        }
    }

    @Override
    public Single<UserData> loginAttempt(String token) {
        mAuthorizationStorage.setUserAccessToken(token);
        return getCurrentLoginInfo();
    }

    @Override
    public DefaultPreferences getSharedPreferences() {
        return mDefaultPreferences;
    }

    private LoggedStatus getCurrentLoggedStatus() {
        String token = mAuthorizationStorage.getUserAccessToken();
        if (token != null && !token.equals("")) {
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

    private Single<StreamsRequest> fetchAdditionalInfo(final StreamsRequest streamsRequest) {
        //makes deep copy of streamsRequest, modifies its "data" field (List<Streams>)
        Gson gson = new Gson();
        final StreamsRequest streamsRequestCopy =
                gson.fromJson(gson.toJson(streamsRequest, StreamsRequest.class), StreamsRequest.class);

        return fetchAdditionalInfo(streamsRequestCopy.getData(), false)
                .flatMap(streams -> {
                    streamsRequestCopy.setData(streams);
                    return Single.just(streamsRequestCopy);
                });

    }

    private Single<List<Stream>> fetchAdditionalInfo(final List<Stream> streamList,
                                                     boolean forceUpdateDb) {
        //makes deep copy of streamList, modifies each element with fetched userdata
        Gson gson = new Gson();
        final List<Stream> streamListCopy = new ArrayList<>();
        for (Stream item : streamList) {
            streamListCopy.add(gson.fromJson(gson.toJson(item), Stream.class));
        }
        final Map<String, Integer> streamsIndexes = new HashMap<>();
        for (int i = 0; i < streamListCopy.size(); i++) {
            streamsIndexes.put(streamListCopy.get(i).getUserId(), i);
        }

        // 1. tries to fetch UserData from db by Id, updates streamListCopy with that data
        // 2. for all streams id's that are not in db, performs net request, updates streamListCopy
        // 3. writes fetched UserData from step 2 to db.
        // if forceUpdateDb == true - skips step 1.
        // result - updated streamListCopy

        Observable<UserData> localSource;
        if (!forceUpdateDb) {
            localSource = Observable
                    .fromIterable(streamListCopy)
                    .subscribeOn(Schedulers.io())
                    .map(Stream::getUserId)
                    .flatMapMaybe(mLocalRepository::findUserDataById)
                    .doOnEach(userDataNotification -> {
                        UserData value = userDataNotification.getValue();
                        if (value != null) {
                            Integer index = streamsIndexes.get(value.getId());
                            if (index != null) {
                                streamListCopy
                                        .get(index)
                                        .setUserData(userDataNotification.getValue());
                            }
                        }
                    });
        } else {
            localSource = Observable.empty();
        }

        Observable<UserData> remoteSource = Observable
                .fromIterable(streamListCopy)
                .filter(stream -> (stream.getUserData() == null || stream.getUserData().isEmpty()))
                .toList()
                .flatMap(mRemoteRepository::getUserDataListByStreamList)
                .flatMapObservable(Observable::fromIterable)
                .doOnEach(userDataNotification -> {
                    UserData value = userDataNotification.getValue();
                    if (value != null) {
                        mLocalRepository.insertUserData(userDataNotification.getValue());
                        Integer index = streamsIndexes.get(value.getId());
                        if (index != null) {
                            streamListCopy
                                    .get(index)
                                    .setUserData(userDataNotification.getValue());
                        }
                    }
                });

        return localSource
                .concatWith(remoteSource)
                .toList()
                .flatMap(userDataList -> Single.just(streamListCopy));

    }
}
