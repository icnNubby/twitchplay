package ru.nubby.playstream.data;

import android.content.Context;

import com.google.gson.Gson;

import javax.inject.Singleton;

import androidx.room.Room;
import dagger.Module;
import dagger.Provides;
import ru.nubby.playstream.data.database.AppDatabase;
import ru.nubby.playstream.data.database.FollowRelationsDao;
import ru.nubby.playstream.data.database.LocalRepository;
import ru.nubby.playstream.data.database.RoomLocalDataSource;
import ru.nubby.playstream.data.database.UserDataDao;
import ru.nubby.playstream.data.twitchapi.RemoteRepository;
import ru.nubby.playstream.data.twitchapi.TwitchRepository;

@Module
public class DataModule {

    private static final String DATABASE_NAME = "playstream";

    @Provides
    @Singleton
    public AppDatabase provideDb(Context context) {
        return Room.databaseBuilder(context.getApplicationContext(),
                AppDatabase.class,
                DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .build();
    }

    @Provides
    @Singleton
    public FollowRelationsDao provideFollowRelationsDao(AppDatabase db) {
        return db.followRelationsDao();
    }

    @Provides
    @Singleton
    public UserDataDao provideUserDataDao(AppDatabase db) {
        return db.userDataDao();
    }

    @Provides
    @Singleton
    public LocalRepository provideLocalRepository(RoomLocalDataSource roomLocalDataSource) {
        return roomLocalDataSource;
    }

    @Provides
    @Singleton
    public RemoteRepository provideRemoteRepository(TwitchRepository twitchRepository) {
        return twitchRepository;
    }

    @Provides
    @Singleton
    public Repository provideRepository(ProxyRepository repository) {
        return repository;
    }

    @Provides
    @Singleton
    public Gson provideDefaultGson() {
        return new Gson();
    }
}