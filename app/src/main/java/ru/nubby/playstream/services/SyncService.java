package ru.nubby.playstream.services;

import android.app.NotificationManager;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import ru.nubby.playstream.data.Repository;

public class SyncService extends JobService {

    private static final int JOB_ID = 12312;

    @Inject
    Repository mRepository;

    public static void schedule(Context context) {
        ComponentName service = new ComponentName(context, SyncService.class);
        new JobInfo.Builder(JOB_ID, service);

    }

    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
        super.onCreate();
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
