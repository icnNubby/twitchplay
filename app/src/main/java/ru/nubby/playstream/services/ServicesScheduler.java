package ru.nubby.playstream.services;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;

import javax.inject.Inject;
import javax.inject.Singleton;

import androidx.annotation.NonNull;

import static android.content.Context.JOB_SCHEDULER_SERVICE;

@Singleton
public class ServicesScheduler {

    private static final String TAG = ServicesScheduler.class.getSimpleName();

    private static final long NOTIFICATIONS_INTERVAL_MILLIS = 1000 * 60 * 15; //todo prefs
    private static final long SYNC_INTERVAL_MILLIS = 1000 * 60 * 60 * 24; //DAY

    private static final int NOTIFICATION_JOB_ID = 1;
    private static final int USERDATA_SYNC_JOB_ID = 2;

    private Context mContext;

    @Inject
    public ServicesScheduler(@NonNull Context context) {
        mContext = context;
    }

    public void scheduleNotifications() {
        ComponentName service = new ComponentName(mContext, NotificationService.class);
        JobInfo jobInfo = new JobInfo.Builder(NOTIFICATION_JOB_ID, service)
                .setPeriodic(NOTIFICATIONS_INTERVAL_MILLIS)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)
                .build();
        JobScheduler scheduler = (JobScheduler) mContext.getSystemService(JOB_SCHEDULER_SERVICE);
        int isJobScheduled = scheduler.schedule(jobInfo);
        if (isJobScheduled == JobScheduler.RESULT_FAILURE) {
            Log.e(TAG, "Notification job has not been scheduled");
        } else {
            Log.d(TAG, "Notification job is scheduled");
        }
    }

    public void cancelNotifications() {
        JobScheduler scheduler = (JobScheduler) mContext.getSystemService(JOB_SCHEDULER_SERVICE);
        scheduler.cancel(NOTIFICATION_JOB_ID);
    }

    public void scheduleUserDataSync() {
        ComponentName service = new ComponentName(mContext, SyncUserDataService.class);
        JobInfo jobInfo = new JobInfo.Builder(USERDATA_SYNC_JOB_ID, service)
                .setPeriodic(SYNC_INTERVAL_MILLIS)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)
                .build();
        JobScheduler scheduler = (JobScheduler) mContext.getSystemService(JOB_SCHEDULER_SERVICE);
        int isJobScheduled = scheduler.schedule(jobInfo);
        if (isJobScheduled == JobScheduler.RESULT_FAILURE) {
            Log.e(TAG, "Sync job has not been scheduled");
        } else {
            Log.d(TAG, "Sync job is scheduled");
        }
    }

    public void cancelUserDataSync() {
        JobScheduler scheduler = (JobScheduler) mContext.getSystemService(JOB_SCHEDULER_SERVICE);
        scheduler.cancel(USERDATA_SYNC_JOB_ID);
    }
}
