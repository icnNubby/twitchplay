package ru.nubby.playstream.data;

import android.content.Context;

import com.google.gson.Gson;

import javax.inject.Singleton;

import androidx.room.Room;
import dagger.Module;
import dagger.Provides;
import ru.nubby.playstream.data.sources.database.GamesDao;
import ru.nubby.playstream.domain.FollowsRepository;
import ru.nubby.playstream.data.repositories.follows.FollowsRepositoryImpl;
import ru.nubby.playstream.data.repositories.streams.StreamsRepositoryImpl;
import ru.nubby.playstream.domain.StreamsRepository;
import ru.nubby.playstream.domain.UsersRepository;
import ru.nubby.playstream.data.repositories.users.UsersRepositoryImpl;
import ru.nubby.playstream.data.sources.database.AppDatabase;
import ru.nubby.playstream.data.sources.database.FollowRelationsDao;
import ru.nubby.playstream.data.sources.database.LocalRepository;
import ru.nubby.playstream.data.sources.database.RoomLocalDataSource;
import ru.nubby.playstream.data.sources.database.UserDataDao;
import ru.nubby.playstream.data.sources.twitchapi.RemoteRepository;
import ru.nubby.playstream.data.sources.twitchapi.TwitchRepository;

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
    public GamesDao provideGamesDao(AppDatabase db) {
        return db.gamesDao();
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
    public StreamsRepository provideRepository(StreamsRepositoryImpl repository) {
        return repository;
    }

    @Provides
    @Singleton
    public UsersRepository provideUsersRepository(UsersRepositoryImpl repository) {
        return repository;
    }

    @Provides
    @Singleton
    public FollowsRepository provideFollowsRepository(FollowsRepositoryImpl repository) {
        return repository;
    }


    @Provides
    @Singleton
    public Gson provideDefaultGson() {
        return new Gson();
    }
}
