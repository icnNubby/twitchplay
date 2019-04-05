package ru.nubby.playstream.services;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import io.reactivex.disposables.Disposable;
import ru.nubby.playstream.domain.UsersRepository;
import ru.nubby.playstream.utils.RxSchedulersProvider;

public class SyncUserDataService extends JobService {

    private static final String TAG = SyncUserDataService.class.getSimpleName();

    @Inject
    UsersRepository mRepository;

    @Inject
    RxSchedulersProvider mSchedulersProvider;

    private Disposable mSyncTask;

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
