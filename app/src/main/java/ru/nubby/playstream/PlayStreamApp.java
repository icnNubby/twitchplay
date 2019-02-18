package ru.nubby.playstream;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

import ru.nubby.playstream.data.GlobalRepository;
import ru.nubby.playstream.data.database.AppDatabase;
import ru.nubby.playstream.data.database.LocalDataSourceImpl;
import ru.nubby.playstream.data.twitchapi.RemoteRepository;
import ru.nubby.playstream.utils.SharedPreferencesHelper;

public class PlayStreamApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(this);
        SharedPreferencesHelper.init(this);
        AppDatabase.init(this);
        GlobalRepository.init(new RemoteRepository(),
                new LocalDataSourceImpl(AppDatabase.getInstance().appDao()));
        //TODO fking use dagger already
    }
}
