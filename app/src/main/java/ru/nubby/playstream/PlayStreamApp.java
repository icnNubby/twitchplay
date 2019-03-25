package ru.nubby.playstream;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import com.squareup.leakcanary.LeakCanary;

import androidx.preference.PreferenceManager;
import dagger.android.AndroidInjector;
import dagger.android.DaggerApplication;
import ru.nubby.playstream.di.components.DaggerAppComponent;
import ru.nubby.playstream.services.SyncUserDataService;

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
        initNotificationChannels();
    }

    private void initNotificationChannels() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || notificationManager == null) {
            return;
        }

        notificationManager.createNotificationChannel(
                new NotificationChannel(getString(R.string.live_streamer_notification_id),
                        "New Streamer is live",
                        NotificationManager.IMPORTANCE_LOW)
        );
    }

    private void setupSyncService() {
        SyncUserDataService.schedule(this);
    }

    @Override
    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
         return DaggerAppComponent
                 .builder()
                 .application(this)
                 .build();
    }
}
