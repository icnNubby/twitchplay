package ru.nubby.playstream.data.repositories.streams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import androidx.annotation.NonNull;
import io.reactivex.Observable;
import io.reactivex.Single;
import ru.nubby.playstream.data.sources.sharedprefs.PersistentStorage;
import ru.nubby.playstream.data.sources.twitchapi.RemoteRepository;
import ru.nubby.playstream.domain.FollowsRepository;
import ru.nubby.playstream.domain.GamesRepository;
import ru.nubby.playstream.domain.StreamsRepository;
import ru.nubby.playstream.domain.UsersRepository;
import ru.nubby.playstream.domain.entities.Game;
import ru.nubby.playstream.domain.entities.Pagination;
import ru.nubby.playstream.domain.entities.Quality;
import ru.nubby.playstream.domain.entities.Stream;
import ru.nubby.playstream.domain.entities.StreamsResponse;
import ru.nubby.playstream.domain.entities.UserData;
import ru.nubby.playstream.domain.interactors.AuthInteractor;
import ru.nubby.playstream.utils.RxSchedulersProvider;

/**
 * Contains streams retrieving logic.
 */
@Singleton
public class StreamsRepositoryImpl implements StreamsRepository {
    private final String TAG = StreamsRepositoryImpl.class.getSimpleName();

    private final RemoteRepository mRemoteRepository;
    private final FollowsRepository mFollowsRepository;
    private final PersistentStorage mPersistentStorage;
    private final GamesRepository mGamesRepository;
    private final UsersRepository mUsersRepository;

    @Inject
    public StreamsRepositoryImpl(@NonNull RemoteRepository remoteRepository,
                                 @NonNull FollowsRepository followsRepository,
                                 @NonNull GamesRepository gamesRepository,
                                 @NonNull UsersRepository usersRepository,
                                 @NonNull PersistentStorage persistentStorage) {

        mRemoteRepository = remoteRepository;
        mFollowsRepository = followsRepository;
        mGamesRepository = gamesRepository;
        mPersistentStorage = persistentStorage;
        mUsersRepository = usersRepository;
    }

    @Override
    public Single<StreamsResponse> getTopStreams() {
        return mRemoteRepository
                .getTopStreams()
                .flatMap(this::fetchUserInfo)
                .flatMap(this::fetchGameInfo);
    }

    @Override
    public Single<StreamsResponse> getTopStreams(Pagination pagination) {
        return mRemoteRepository
                .getTopStreams(pagination)
                .flatMap(this::fetchUserInfo)
                .flatMap(this::fetchGameInfo);
    }

    @Override
    public Single<List<Stream>> getLiveStreamsFollowedByUser(UserData userData) {
        return mRemoteRepository
                .getLiveStreamsFromRelationList(
                        mFollowsRepository.getUserFollows(userData.getId()))
                .flatMap(this::fetchUserInfo)
                .flatMap(this::fetchGameInfo)
                .doOnSuccess(this::saveLiveStreamList);
    }

    @Override
    public Single<HashMap<Quality, String>> getQualityUrls(Stream stream) {
        return mRemoteRepository
                .getQualityUrls(stream);
    }

    @Override
    public Observable<Stream> getUpdatableStreamInfo(Stream stream) {
        return mRemoteRepository
                .getUpdatedStreamInfo(stream);
    }

    private Single<StreamsResponse> fetchUserInfo(final StreamsResponse streamsResponse) {
        //makes deep copy of streamsResponse, modifies its "data" field (List<Streams>)
        final StreamsResponse streamsResponseCopy = new StreamsResponse(streamsResponse);

        return fetchUserInfo(streamsResponseCopy.getData())
                .flatMap(streams -> {
                    streamsResponseCopy.setData(streams);
                    return Single.just(streamsResponseCopy);
                });

    }

    private Single<List<Stream>> fetchUserInfo(final List<Stream> streamList) {
        //makes deep copy of streamList
        final List<Stream> streamListCopy = new ArrayList<>();
        for (Stream item : streamList) {
            streamListCopy.add(new Stream(item));
        }

        return Observable
                .fromIterable(streamList)
                .map(Stream::getUserId)
                .toList()
                .flatMap(mUsersRepository::getUsersByIds)
                .flatMap(users -> {
                    HashMap<String, UserData> usersMap = new HashMap<>();
                    for (UserData user : users) {
                        usersMap.put(user.getId(), user);
                    }
                    for (Stream stream : streamListCopy) {
                        UserData userForStream = usersMap.get(stream.getUserId());
                        if (userForStream != null) {
                            stream.setUserData(userForStream);
                        } else {
                            stream.setUserData(new UserData());
                        }
                    }
                    return Single.just(streamListCopy);
                });

    }

    private Single<StreamsResponse> fetchGameInfo(final StreamsResponse streamsResponse) {
        //makes deep copy of streamsResponse, modifies its "data" field (List<Streams>)
        final StreamsResponse streamsResponseCopy = new StreamsResponse(streamsResponse);

        return fetchGameInfo(streamsResponseCopy.getData())
                .flatMap(streams -> {
                    streamsResponseCopy.setData(streams);
                    return Single.just(streamsResponseCopy);
                });

    }

    private Single<List<Stream>> fetchGameInfo(final List<Stream> streamList) {
        //makes deep copy of streamList
        final List<Stream> streamListCopy = new ArrayList<>();
        for (Stream item : streamList) {
            streamListCopy.add(new Stream(item));
        }

        return Observable
                .fromIterable(streamList)
                .map(Stream::getGameId)
                .toList()
                .flatMap(mGamesRepository::getGamesByIds)
                .flatMap(games -> {
                    HashMap<String, Game> gamesMap = new HashMap<>();
                    for (Game game : games) {
                        gamesMap.put(game.getId(), game);
                    }
                    for (Stream stream : streamListCopy) {
                        Game gameForStream = gamesMap.get(stream.getGameId());
                        if (gameForStream != null) {
                            stream.setGame(gameForStream);
                        } else {
                            stream.setGame(new Game());
                        }
                    }
                    return Single.just(streamListCopy);
                });

    }

    //todo maybe persist data in db, if we will have big lists, it will be better
    private void saveLiveStreamList(List<Stream> streamList) {
        mPersistentStorage.setStreamList(streamList);
    }
}
