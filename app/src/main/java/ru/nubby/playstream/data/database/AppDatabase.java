package ru.nubby.playstream.data.database;

import android.content.Context;
import android.util.Log;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import ru.nubby.playstream.model.FollowRelations;

@Database(entities = {FollowRelations.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static final String TAG = AppDatabase.class.getSimpleName();
    private static final Object LOCK = new Object();
    private static final String DATABASE_NAME = "playstream";
    private static AppDatabase sInstance;

    public static void init(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {
                if (sInstance == null) {
                    Log.d(TAG, "Creating new database instance");
                    sInstance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class,
                            AppDatabase.DATABASE_NAME)
                            .build();
                }
            }
        }
    }

    public static AppDatabase getInstance() {
        if (sInstance == null) {
            throw new NullPointerException("App database not instantiated.");
        }
        Log.d(TAG, "Getting the database instance");
        return sInstance;
    }

    public abstract FollowRelationsDao appDao();

}

