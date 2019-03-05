package ru.nubby.playstream;

import com.squareup.leakcanary.LeakCanary;

import androidx.preference.PreferenceManager;
import dagger.android.AndroidInjector;
import dagger.android.DaggerApplication;
import ru.nubby.playstream.domain.ProxyRepository;
import ru.nubby.playstream.domain.database.AppDatabase;
import ru.nubby.playstream.domain.database.RoomLocalDataSource;
import ru.nubby.playstream.domain.sharedprefs.DefaultPreferences;
import ru.nubby.playstream.domain.twitchapi.RemoteRepository;
import ru.nubby.playstream.domain.sharedprefs.AuthorizationStorage;
import ru.nubby.playstream.domain.twitchapi.TwitchApiModule;

public class PlayStreamApp extends DaggerApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(this);
        ProxyRepository.init(
                new RemoteRepository(TwitchApiModule.getInstance()),
                new RoomLocalDataSource(
                        AppDatabase.getInstance().followRelationsDao(),
                        AppDatabase.getInstance().userDataDao()),
                new AuthorizationStorage(this),
                new DefaultPreferences(this));

        PreferenceManager.setDefaultValues(this, R.xml.pref_display, false);

        //TODO fking use dagger already
    }

    @Override
    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
        return DaggerAppComponent;
    }
}
