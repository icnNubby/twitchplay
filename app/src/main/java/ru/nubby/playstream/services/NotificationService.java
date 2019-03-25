package ru.nubby.playstream.services;

import android.app.NotificationManager;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import ru.nubby.playstream.data.Repository;
import ru.nubby.playstream.model.Stream;
import ru.nubby.playstream.utils.RxSchedulersProvider;

public class NotificationService extends JobService {

    private static final String TAG = NotificationService.class.getSimpleName();

    private static final long CHECK_INTERVAL_MILLIS = 1000 * 60 * 15; //todo prefs
    private static final int JOB_ID = 11112;

    @Inject
    Repository mRepository;

    @Inject
    RxSchedulersProvider mSchedulersProvider;

    Picasso mPicasso;

    private Disposable mRetrieveLiveStreams;

    public static void schedule(Context context) {
        ComponentName service = new ComponentName(context, SyncUserDataService.class);
        JobInfo jobInfo = new JobInfo.Builder(JOB_ID, service)
                .setPeriodic(CHECK_INTERVAL_MILLIS)
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
        mPicasso = Picasso.get();
    }

    @Override
    public boolean onStartJob(JobParameters params) {

        final List<Stream> lastStreams = getLastLiveStreams();
        mRetrieveLiveStreams = getLiveStreams()
                .subscribeOn(mSchedulersProvider.getIoScheduler())
                .observeOn(mSchedulersProvider.getUiScheduler())
                .map(streams -> compareAndGetFreshStreams(lastStreams, streams))
                .subscribe(
                        freshStreams -> {
                            constructNotifications(freshStreams);
                            jobFinished(params, false);
                        },
                        error -> {
                            Log.e(TAG, "Cant execute notification job", error);
                            jobFinished(params, true);
                        });
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        if (mRetrieveLiveStreams != null) {
            mRetrieveLiveStreams.dispose();
        }
        return false;
    }

    private List<Stream> getLastLiveStreams() {
        List<Stream> streams = mRepository.getLastStreamList();
        for (Stream element: streams) {
            Log.d(TAG, element.getStreamerName());
        }
        return streams;
    }

    private Single<List<Stream>> getLiveStreams() {
        return mRepository.getLiveStreamsFollowedByUser();
    }

    private List<Stream> compareAndGetFreshStreams(List<Stream> oldStreams,
                                                   List<Stream> newStreams) {
        List<Stream> outList = new ArrayList<>();
        for (Stream newElement: newStreams) {
            if (!oldStreams.contains(newElement)) {
                outList.add(newElement);
            }
        }
        return outList;
    }

    private void constructNotifications(List<Stream> freshStreams) {
        if (freshStreams.isEmpty()) return;
        NotificationManager notificationManager = (NotificationManager)
                getApplicationContext().getSystemService(NOTIFICATION_SERVICE);

    }

}
