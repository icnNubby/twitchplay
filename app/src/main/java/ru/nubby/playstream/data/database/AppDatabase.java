package ru.nubby.playstream.data.database;

import android.content.Context;
import android.util.Log;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import ru.nubby.playstream.model.FollowRelations;
import ru.nubby.playstream.model.UserData;

@Database(entities = {FollowRelations.class, UserData.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static final String TAG = AppDatabase.class.getSimpleName();
    private static final String DATABASE_NAME = "playstream";
    private static AppDatabase sInstance;

    public static synchronized void init(Context context) {
        if (sInstance == null) {
            Log.d(TAG, "Creating new database instance");
            sInstance = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class,
                    AppDatabase.DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }
    }

    public static AppDatabase getInstance() {
        if (sInstance == null) {
            throw new NullPointerException("App database not instantiated.");
        }
        Log.d(TAG, "Getting the database instance");
        return sInstance;
    }

    public abstract FollowRelationsDao followRelationsDao();

    public abstract UserDataDao userDataDao();

}

