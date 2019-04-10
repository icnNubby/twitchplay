package ru.nubby.playstream.data.sources.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import ru.nubby.playstream.data.sources.database.dao.FollowRelationsDao;
import ru.nubby.playstream.data.sources.database.dao.GamesDao;
import ru.nubby.playstream.data.sources.database.dao.UserDataDao;
import ru.nubby.playstream.domain.entities.FollowRelations;
import ru.nubby.playstream.domain.entities.Game;
import ru.nubby.playstream.domain.entities.UserData;

@Database(
        entities = {FollowRelations.class, UserData.class, Game.class},
        version = 3,
        exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract FollowRelationsDao followRelationsDao();

    public abstract UserDataDao userDataDao();

    public abstract GamesDao gamesDao();

}

