package ru.nubby.playstream;

import com.squareup.leakcanary.LeakCanary;

import androidx.preference.PreferenceManager;
import dagger.android.AndroidInjector;
import dagger.android.DaggerApplication;
import ru.nubby.playstream.di.components.DaggerAppComponent;

public class PlayStreamApp extends DaggerApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(this);
        PreferenceManager.setDefaultValues(this, R.xml.pref_display, false);
    }

    @Override
    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
         return DaggerAppComponent
                 .builder()
                 .application(this)
                 .build();
    }
}
