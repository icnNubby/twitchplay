package ru.nubby.playstream.data.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import ru.nubby.playstream.domain.entities.FollowRelations;
import ru.nubby.playstream.domain.entities.UserData;

@Database(entities = {FollowRelations.class, UserData.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract FollowRelationsDao followRelationsDao();

    public abstract UserDataDao userDataDao();

}

