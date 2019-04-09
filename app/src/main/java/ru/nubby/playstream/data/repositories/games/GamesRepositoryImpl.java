package ru.nubby.playstream.data.repositories.games;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Single;
import ru.nubby.playstream.data.sources.database.LocalRepository;
import ru.nubby.playstream.data.sources.twitchapi.RemoteRepository;
import ru.nubby.playstream.domain.GamesRepository;
import ru.nubby.playstream.domain.entities.Game;
import ru.nubby.playstream.domain.entities.GamesResponse;
import ru.nubby.playstream.domain.entities.Pagination;

@Singleton
public class GamesRepositoryImpl implements GamesRepository {

    private static final String TAG = GamesRepositoryImpl.class.getSimpleName();
    private final LocalRepository mLocalRepository;
    private final RemoteRepository mRemoteRepository;

    @Inject
    public GamesRepositoryImpl(LocalRepository localRepository,
                               RemoteRepository remoteRepository) {
        mLocalRepository = localRepository;
        mRemoteRepository = remoteRepository;
    }

    @Override
    public Single<GamesResponse> getTopGames() {
        return mRemoteRepository.getTopGames();
    }

    @Override
    public Single<GamesResponse> getTopGames(Pagination pagination) {
        return mRemoteRepository.getTopGames(pagination);
    }

    @Override
    public Single<List<Game>> getGamesByIds(List<String> gamesIds) {
        final ArrayList<String> locallyFetched = new ArrayList<>();
        Single<List<Game>> local = mLocalRepository
                .findGames(gamesIds)
                .toSingle()
                .doOnSuccess(games -> {
                    for (Game item : games) {
                        if (item != null) {
                            locallyFetched.add(item.getId());
                        }
                    }
                });

        Single<List<Game>> remote =
                Single.defer(() -> mRemoteRepository
                        .getGamesByIds(getUnfetchedIds(gamesIds, locallyFetched)))
                        .flatMap(games ->
                                mLocalRepository
                                        .insertGameList(games.toArray(new Game[0]))
                                        .andThen(Single.just(games)));

        return local
                .concatWith(remote)
                .flatMapIterable(items -> items)
                .toObservable()
                .toList();
    }

    private List<String> getUnfetchedIds(List<String> allIds, List<String> alreadyFetched) {
        HashMap<String, Void> allIdsMapped = new HashMap<>();
        for (String id : allIds) {
            allIdsMapped.put(id, null);
        }
        for (String id : alreadyFetched) {
            allIdsMapped.remove(id);
        }
        return new ArrayList<>(allIdsMapped.keySet());
    }

}
