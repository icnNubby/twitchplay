package ru.nubby.playstream;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import com.squareup.leakcanary.LeakCanary;

import javax.inject.Inject;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.preference.PreferenceManager;
import dagger.android.AndroidInjector;
import dagger.android.DaggerApplication;
import ru.nubby.playstream.di.components.DaggerAppComponent;
import ru.nubby.playstream.services.ServicesScheduler;

public class PlayStreamApp extends DaggerApplication implements LifecycleObserver {

    @Inject
    ServicesScheduler mServicesScheduler;

    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(this);
        PreferenceManager.setDefaultValues(this, R.xml.pref_display, false);
        initNotificationChannels();
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
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

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onAppBackgrounded() {
        mServicesScheduler.scheduleUserDataSync();
        mServicesScheduler.scheduleNotifications();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onAppForegrounded() {
        mServicesScheduler.cancelNotificaions();
    }


    @Override
    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
         return DaggerAppComponent
                 .builder()
                 .application(this)
                 .build();
    }
}
