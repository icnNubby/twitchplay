package ru.nubby.playstream;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

import ru.nubby.playstream.data.GlobalRepository;
import ru.nubby.playstream.data.database.AppDatabase;
import ru.nubby.playstream.data.database.RoomLocalDataSource;
import ru.nubby.playstream.data.twitchapi.RemoteRepository;
import ru.nubby.playstream.data.sharedprefs.SharedPreferencesManager;

public class PlayStreamApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(this);
        AppDatabase.init(this);
        GlobalRepository.init(
                new RemoteRepository(),
                new RoomLocalDataSource(
                        AppDatabase.getInstance().followRelationsDao(),
                        AppDatabase.getInstance().userDataDao()),
                new SharedPreferencesManager(this));
        //TODO fking use dagger already
    }
}
