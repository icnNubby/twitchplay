package ru.nubby.playstream.domain;

import java.util.List;

import io.reactivex.Single;
import ru.nubby.playstream.domain.entities.GamesResponse;
import ru.nubby.playstream.domain.entities.Pagination;

public interface GamesRepository {

    /**
     * Gets games list from remote repository
     * @return list of top games
     */
    Single<GamesResponse> getTopGames();

    /**
     * Gets games list from remote repository
     * @param pagination pagination cursor
     * @return list of games after pagination cursor
     */
    Single<GamesResponse> getTopGames(Pagination pagination);

    /**
     * Gets games list by their ids.
     * @param gamesIds list of id's
     * @return server response with games objects
     */
    Single<GamesResponse> getGamesByIds(List<String> gamesIds);

}
