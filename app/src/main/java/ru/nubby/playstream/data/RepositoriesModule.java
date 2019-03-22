package ru.nubby.playstream.data;

import android.content.Context;

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
import ru.nubby.playstream.data.twitchapi.services.RawJsonService;
import ru.nubby.playstream.data.twitchapi.services.TwitchApiService;
import ru.nubby.playstream.data.twitchapi.services.TwitchHelixService;
import ru.nubby.playstream.data.twitchapi.services.TwitchKrakenService;

@Module
public class RepositoriesModule {

    private static final String DATABASE_NAME = "playstream";

    @Provides
    @Singleton
    public RemoteRepository provideRemoteRepository(RawJsonService rawJsonService,
                                                    TwitchApiService twitchApiService,
                                                    TwitchKrakenService twitchKrakenService,
                                                    TwitchHelixService twitchHelixService) {
        return new RemoteRepository(rawJsonService,
                twitchApiService,
                twitchKrakenService,
                twitchHelixService);
    }

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
    public LocalRepository provideLocalRepository(FollowRelationsDao followRelationsDao,
                                                  UserDataDao userDataDao) {
        return new RoomLocalDataSource(followRelationsDao, userDataDao);
    }

    @Provides
    @Singleton
    public Repository provideRepository(ProxyRepository repository) {
        return repository;
    }
}

//TODO move it?