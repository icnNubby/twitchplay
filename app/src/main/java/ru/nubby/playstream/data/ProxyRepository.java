package ru.nubby.playstream.data;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import androidx.annotation.NonNull;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import ru.nubby.playstream.data.database.LocalRepository;
import ru.nubby.playstream.data.sharedprefs.AuthorizationStorage;
import ru.nubby.playstream.data.sharedprefs.DefaultPreferences;
import ru.nubby.playstream.data.sharedprefs.PersistentStorage;
import ru.nubby.playstream.data.twitchapi.RemoteRepository;
import ru.nubby.playstream.domain.entity.FollowRelations;
import ru.nubby.playstream.domain.entity.Pagination;
import ru.nubby.playstream.domain.entity.Quality;
import ru.nubby.playstream.domain.entity.Stream;
import ru.nubby.playstream.domain.entity.StreamListNavigationState;
import ru.nubby.playstream.domain.entity.StreamsResponse;
import ru.nubby.playstream.domain.entity.UserData;
import ru.nubby.playstream.domain.interactor.AuthInteractor;

/**
 * Contains decision making on what kind of repo we should use.
 * Some logic definitely can be decoupled into usecases/interactors.
 * Although its already some sort of interactor.
 */
//todo split into some usecases(or logically connected entities ex. UsersInteractor, StreamsInteractor, etc.)

@Singleton
public class ProxyRepository implements Repository {
    private final String TAG = ProxyRepository.class.getSimpleName();

    private final RemoteRepository mRemoteRepository;
    private final LocalRepository mLocalRepository;
    private final AuthorizationStorage mAuthorizationStorage;
    private final PersistentStorage mPersistentStorage;
    private final AuthInteractor mAuthInteractor;

    private boolean mFollowsFullUpdate = true; //TODO IDK implement in some other way its too hacky

    @Inject
    public ProxyRepository(@NonNull RemoteRepository remoteRepository,
                           @NonNull LocalRepository localRepository,
                           @NonNull AuthorizationStorage authorizationStorage,
                           @NonNull PersistentStorage persistentStorage,
                           @NonNull AuthInteractor authInteractor) {

        mRemoteRepository = remoteRepository;
        mLocalRepository = localRepository;
        mAuthorizationStorage = authorizationStorage;
        mPersistentStorage = persistentStorage;
        mAuthInteractor = authInteractor;
    }

    @Override
    public Single<StreamsResponse> getTopStreams() {
        return mRemoteRepository
                .getTopStreams()
                .flatMap(this::fetchAdditionalInfo)
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Single<StreamsResponse> getTopStreams(Pagination pagination) {
        return mRemoteRepository
                .getTopStreams(pagination)
                .flatMap(this::fetchAdditionalInfo)
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
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

    @Override
    public Single<List<Stream>> getLiveStreamsFollowedByUser() {
        return mAuthInteractor
                .getCurrentLoginInfo()
                .subscribeOn(Schedulers.io())
                .flatMap(userData -> mRemoteRepository
                        .getLiveStreamsFromRelationList(getUserFollows(userData.getId())))
                .flatMap(streams -> this.fetchAdditionalInfo(streams, false))
                .doOnSuccess(this::saveLiveStreamList)
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
        return mAuthInteractor
                .getCurrentLoginInfo()
                .flatMapCompletable(
                        userData ->
                                mRemoteRepository
                                        .followTargetUser(
                                                mAuthorizationStorage.getUserAccessToken(),
                                                userData.getId(),
                                                targetStream.getUserId())
                                        .andThen(mLocalRepository
                                                .insertFollowRelationsEntry(
                                                        new FollowRelations(userData.getId(),
                                                                userData.getLogin(),
                                                                targetStream.getUserId(),
                                                                targetStream.getStreamerLogin(),
                                                                ""))));
        //todo fix empty fields;
    }

    @Override
    public Completable unfollowStream(Stream targetStream) {
        return mAuthInteractor
                .getCurrentLoginInfo()
                .flatMapCompletable(userData ->
                        mRemoteRepository
                                .unfollowTargetUser(mAuthorizationStorage.getUserAccessToken(),
                                        userData.getId(), targetStream.getUserId())
                                .andThen(mLocalRepository
                                        .deleteFollowRelationsEntry(
                                                new FollowRelations(userData.getId(),
                                                        userData.getLogin(),
                                                        targetStream.getUserId(),
                                                        targetStream.getStreamerLogin(),
                                                        ""))));
    }

    @Override
    public Single<Boolean> isStreamFollowed(Stream targetStream) {
        return mAuthInteractor
                .getCurrentLoginInfo()
                .flatMap(userData -> mLocalRepository.findRelation(userData.getId(),
                        targetStream.getUserId()))
                .flatMap(followRelationsList ->
                        Single.create(emitter -> emitter.onSuccess(!followRelationsList.isEmpty())));
    }

    @Override
    public Completable synchronizeFollows(String userId) {
        mFollowsFullUpdate = true;
        return getUserFollows(userId)
                .flatMapCompletable(followRelationsList -> CompletableObserver::onComplete);
    }

    @Override
    public Completable synchronizeUserData() {
        return mLocalRepository
                .getAllUserDataEntries()
                .toSingle()
                .flatMap(mRemoteRepository::getUpdatedUserDataList)
                .flatMapCompletable(updatedUserDataList -> mLocalRepository
                        .insertUserDataList(updatedUserDataList.toArray(new UserData[0])));
    }

    @Override
    public List<Stream> getLastStreamList() {
        return mPersistentStorage.getStreamList();
    }

    //Private methods

    private Single<StreamsResponse> fetchAdditionalInfo(final StreamsResponse streamsResponse) {
        //makes deep copy of streamsResponse, modifies its "data" field (List<Streams>)
        Gson gson = new Gson();
        final StreamsResponse streamsResponseCopy =
                gson.fromJson(gson.toJson(streamsResponse, StreamsResponse.class), StreamsResponse.class);

        return fetchAdditionalInfo(streamsResponseCopy.getData(), false)
                .flatMap(streams -> {
                    streamsResponseCopy.setData(streams);
                    return Single.just(streamsResponseCopy);
                });

    }

    private Single<List<Stream>> fetchAdditionalInfo(final List<Stream> streamList,
                                                     boolean forceUpdateDb) {
        //makes deep copy of streamList
        Gson gson = new Gson();
        final List<Stream> streamListCopy = new ArrayList<>();
        for (Stream item : streamList) {
            streamListCopy.add(gson.fromJson(gson.toJson(item), Stream.class));
        }
        final Map<String, Integer> streamsIndexes = new HashMap<>();
        for (int i = 0; i < streamListCopy.size(); i++) {
            streamsIndexes.put(streamListCopy.get(i).getUserId(), i);
        }

        /*
             1. tries to fetch UserData from db by Id, updates streamListCopy with that data
             2. for all streams id's that are not in db, performs net request, updates
                streamListCopy
             3. writes fetched UserData from step 2 to db.
             if forceUpdateDb == true - skips step 1.
             result - updated COPY of streamList
         */

        Observable<UserData> localSource;
        if (!forceUpdateDb) {
            localSource = Observable
                    .fromIterable(streamListCopy)
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
                .flatMap(userDataList -> mLocalRepository
                        .insertUserDataList(userDataList.toArray(new UserData[0]))
                        .andThen(Single.just(userDataList)))
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

    //todo maybe persist data in db, if we will have big lists, it will be better
    private void saveLiveStreamList(List<Stream> streamList) {
        mPersistentStorage.setStreamList(streamList);
    }
}
