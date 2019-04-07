package ru.nubby.playstream.data.repositories.games;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Single;
import ru.nubby.playstream.data.sources.database.LocalRepository;
import ru.nubby.playstream.data.sources.twitchapi.RemoteRepository;
import ru.nubby.playstream.domain.GamesRepository;
import ru.nubby.playstream.domain.entities.GamesResponse;
import ru.nubby.playstream.domain.entities.Pagination;

public class GamesRepositoryImpl implements GamesRepository {

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
    public Single<GamesResponse> getGamesByIds(List<String> gamesIds) {
        //todo start here pls
        return mRemoteRepository.getGamesByIds(gamesIds);
    }
}
