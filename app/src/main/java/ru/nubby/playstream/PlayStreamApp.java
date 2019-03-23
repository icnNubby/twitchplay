package ru.nubby.playstream;

import com.squareup.leakcanary.LeakCanary;

import androidx.preference.PreferenceManager;
import dagger.android.AndroidInjector;
import dagger.android.DaggerApplication;
import ru.nubby.playstream.di.components.DaggerAppComponent;
import ru.nubby.playstream.services.SyncService;

public class PlayStreamApp extends DaggerApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(this);
        PreferenceManager.setDefaultValues(this, R.xml.pref_display, false);
        setupSyncService();
    }

    private void setupSyncService() {
        SyncService.schedule(this);
    }

    @Override
    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
         return DaggerAppComponent
                 .builder()
                 .application(this)
                 .build();
    }
}
