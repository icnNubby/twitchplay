package ru.nubby.playstream.di;

import android.content.Context;

import javax.inject.Singleton;

import androidx.room.Room;
import dagger.Module;
import dagger.Provides;
import ru.nubby.playstream.domain.database.AppDatabase;
import ru.nubby.playstream.domain.database.FollowRelationsDao;
import ru.nubby.playstream.domain.database.LocalRepository;
import ru.nubby.playstream.domain.database.RoomLocalDataSource;
import ru.nubby.playstream.domain.database.UserDataDao;
import ru.nubby.playstream.domain.twitchapi.RemoteRepository;
import ru.nubby.playstream.domain.twitchapi.TwitchApiModule;

@Module
public abstract class RepositoriesModule {

    private static final String DATABASE_NAME = "playstream";

    @Provides
    public LocalRepository provideLocalRepository(FollowRelationsDao followRelationsDao,
                                                  UserDataDao userDataDao) {
        return new RoomLocalDataSource(followRelationsDao, userDataDao);
    }

    @Provides
    public RemoteRepository provideRemoteRepository(TwitchApiModule twichApi) {
        return new RemoteRepository(twichApi.getRawJsonHlsService(),
                twichApi.getStreamApiService(),
                twichApi.getKrakenService(),
                twichApi.getStreamHelixService());
    }

    @Singleton
    @Provides
    static AppDatabase provideDb(Context context) {
        return Room.databaseBuilder(context.getApplicationContext(),
                AppDatabase.class,
                DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .build();
    }

    @Singleton
    @Provides
    static FollowRelationsDao provideFollowRelationsDao(AppDatabase db) {
        return db.followRelationsDao();
    }

    @Singleton
    @Provides
    static UserDataDao provideUserDataDao(AppDatabase db) {
        return db.userDataDao();
    }
}
