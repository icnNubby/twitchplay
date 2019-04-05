package ru.nubby.playstream.data.repositories.streams;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import androidx.annotation.NonNull;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.nubby.playstream.domain.FollowsRepository;
import ru.nubby.playstream.domain.StreamsRepository;
import ru.nubby.playstream.data.sources.database.LocalRepository;
import ru.nubby.playstream.data.sources.sharedprefs.PersistentStorage;
import ru.nubby.playstream.data.sources.twitchapi.RemoteRepository;
import ru.nubby.playstream.domain.entities.Pagination;
import ru.nubby.playstream.domain.entities.Quality;
import ru.nubby.playstream.domain.entities.Stream;
import ru.nubby.playstream.domain.entities.StreamsResponse;
import ru.nubby.playstream.domain.entities.UserData;
import ru.nubby.playstream.domain.interactors.AuthInteractor;

/**
 * Contains streams retrieving logic.
 */
@Singleton
public class StreamsRepositoryImpl implements StreamsRepository {
    private final String TAG = StreamsRepositoryImpl.class.getSimpleName();

    private final RemoteRepository mRemoteRepository;
    private final LocalRepository mLocalRepository;
    private final FollowsRepository mFollowsRepository;
    private final PersistentStorage mPersistentStorage;
    private final AuthInteractor mAuthInteractor;

    @Inject
    public StreamsRepositoryImpl(@NonNull RemoteRepository remoteRepository,
                                 @NonNull LocalRepository localRepository,
                                 @NonNull FollowsRepository followsRepository,
                                 @NonNull PersistentStorage persistentStorage,
                                 @NonNull AuthInteractor authInteractor) {

        mRemoteRepository = remoteRepository;
        mLocalRepository = localRepository;
        mFollowsRepository = followsRepository;
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
    public Single<List<Stream>> getLiveStreamsFollowedByUser() {
        return mAuthInteractor
                .getCurrentLoginInfo()
                .subscribeOn(Schedulers.io())
                .flatMap(userData -> mRemoteRepository
                        .getLiveStreamsFromRelationList(
                                mFollowsRepository.getUserFollows(userData.getId())))
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
    public Observable<Stream> getUpdatableStreamInfo(Stream stream) {
        return mRemoteRepository
                .getUpdatedStreamInfo(stream)
                .observeOn(AndroidSchedulers.mainThread());
    }

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
