package ru.nubby.playstream.data.sources.database.dao;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import ru.nubby.playstream.domain.entities.Game;

@Dao
public interface GamesDao {

    @Query("SELECT * FROM games WHERE id = :id")
    Maybe<Game> findGame(String id);

    @Query("SELECT * FROM games WHERE id in (:ids)")
    Maybe<List<Game>> findGames(List<String> ids);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertGame(Game game);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertGameList(Game... game);

    @Delete
    Completable deleteGame(Game game);

    @Query("DELETE FROM games")
    Completable deleteAllGames();

}
