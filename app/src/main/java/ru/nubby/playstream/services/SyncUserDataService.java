package ru.nubby.playstream.services;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import io.reactivex.disposables.Disposable;
import ru.nubby.playstream.data.Repository;
import ru.nubby.playstream.utils.RxSchedulersProvider;

public class SyncUserDataService extends JobService {

    private static final String TAG = SyncUserDataService.class.getSimpleName();

    private static final long SYNC_INTERVAL_MILLIS = 1000 * 60 * 60 * 24; //DAY

    private static final int JOB_ID = 12312;

    @Inject
    Repository mRepository;

    @Inject
    RxSchedulersProvider mSchedulersProvider;

    private Disposable mSyncTask;

    public static void schedule(Context context) {
        ComponentName service = new ComponentName(context, SyncUserDataService.class);
        JobInfo jobInfo = new JobInfo.Builder(JOB_ID, service)
                .setPeriodic(SYNC_INTERVAL_MILLIS)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)
                .build();
        JobScheduler scheduler = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);
        int isJobScheduled = scheduler.schedule(jobInfo);
        if (isJobScheduled == JobScheduler.RESULT_FAILURE) {
            Log.e(TAG, "Sync job has not been scheduled");
        }
    }

    public static void cancel(Context context) {
        JobScheduler scheduler = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);
        scheduler.cancel(JOB_ID);
    }

    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
        super.onCreate();
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        mSyncTask = mRepository
                .synchronizeUserData()
                .subscribeOn(mSchedulersProvider.getIoScheduler())
                .observeOn(mSchedulersProvider.getUiScheduler())
                .subscribe(
                        () -> {
                            jobFinished(params, false);
                            Log.d(TAG, "User data db synched.");
                        },
                        error -> {
                            jobFinished(params, true);
                            Log.e(TAG, "Error while syncing user data db, " +
                                    "reschedule task", error);
                        });
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        if (mSyncTask != null ) {
            mSyncTask.dispose();
        }
        return true;
    }

}
