package ru.nubby.playstream.data;

import android.util.Log;

import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import ru.nubby.playstream.data.database.LocalDataSource;
import ru.nubby.playstream.data.database.LocalDataSourceImpl;
import ru.nubby.playstream.data.twitchapi.RemoteRepository;
import ru.nubby.playstream.model.FollowRelations;
import ru.nubby.playstream.model.Pagination;
import ru.nubby.playstream.model.Quality;
import ru.nubby.playstream.model.Stream;
import ru.nubby.playstream.model.StreamsRequest;
import ru.nubby.playstream.model.UserData;

/**
 * Contains decision making on what kind of repo we should use.
 * Some logic probably can be decoupled into usecases/interactors.
 */
public class GlobalRepository implements Repository {
    private final String TAG = GlobalRepository.class.getSimpleName();

    private static final Object LOCK = new Object();

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
            synchronized (LOCK) {
                if (sInstance == null) {
                    sInstance = new GlobalRepository(remoteRepository, localDataSource);
                }
            }
        }
    }

    public static GlobalRepository getInstance() {
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
                    .doOnSuccess(list -> {
                        mLocalDataSource
                                .insertFollowRelationsList(list.toArray(new FollowRelations[0]))
                                .subscribe(() -> {},
                                        error -> firstLoad = true);
                        //TODO think how to do better, that is awful
                        Log.d(TAG, "Probably written to db");
                    });
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
    public Single<List<Stream>> getLiveStreamsFollowedByUser(String userId) {
        return mRemoteRepository.getLiveStreamsFromRelationList(getUserFollows(userId));
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


}
