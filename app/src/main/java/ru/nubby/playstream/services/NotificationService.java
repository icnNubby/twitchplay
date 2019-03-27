package ru.nubby.playstream.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.Pair;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import androidx.core.app.NotificationCompat;
import dagger.android.AndroidInjection;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import ru.nubby.playstream.R;
import ru.nubby.playstream.data.Repository;
import ru.nubby.playstream.model.Stream;
import ru.nubby.playstream.model.UserData;
import ru.nubby.playstream.presentation.stream.StreamChatActivity;
import ru.nubby.playstream.presentation.streamlist.StreamListActivity;
import ru.nubby.playstream.utils.RxSchedulersProvider;

public class NotificationService extends JobService {

    private static final String TAG = NotificationService.class.getSimpleName();

    private static final String NOTIFICATION_GROUP_KEY = "ru.nubby.playstream.NEW_LIVE_STREAMS";

    private static final long CHECK_INTERVAL_MILLIS = 1000 * 60 * 15; //todo prefs
    private static final int JOB_ID = 11112;
    private static final int NOTIFICATION_SUMMARY = 112112;

    @Inject
    Repository mRepository;

    @Inject
    RxSchedulersProvider mSchedulersProvider;

    @Inject
    Context mContext;

    @Inject
    Gson mGson;

    Picasso mPicasso;

    private Disposable mRetrieveLiveStreams;
    private Disposable mRetrieveAvatarBitmaps;

    public static void schedule(Context context) {
        ComponentName service = new ComponentName(context, NotificationService.class);
        JobInfo jobInfo = new JobInfo.Builder(JOB_ID, service)
                .setPeriodic(CHECK_INTERVAL_MILLIS)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)
                .build();
        JobScheduler scheduler = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);
        int isJobScheduled = scheduler.schedule(jobInfo);
        if (isJobScheduled == JobScheduler.RESULT_FAILURE) {
            Log.e(TAG, "Notification job has not been scheduled");
        } else {
            Log.d(TAG, "Schedule: notification job scheduled");
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
        Log.d(TAG, "onStartJob: starting job");
        final List<Stream> lastStreams = getLastLiveStreams();
        //TODO REMOVE, TESTONLY
        if (!lastStreams.isEmpty()) lastStreams.remove(0);
        if (!lastStreams.isEmpty()) lastStreams.remove(0);
        if (!lastStreams.isEmpty()) lastStreams.remove(0);
        mRetrieveLiveStreams = getLiveStreams()
                .filter(streamList -> !streamList.isEmpty())
                .subscribeOn(mSchedulersProvider.getIoScheduler())
                .observeOn(mSchedulersProvider.getUiScheduler())
                .subscribe(
                        streams -> {
                            Log.d(TAG, "onStartJob: getting fresh and dead streams");
                            List<Stream> freshStreams =
                                    compareAndGetFreshStreams(lastStreams, streams);
                            List<Stream> deadStreams =
                                    compareAndGetDeadStreams(lastStreams, streams);

                            constructNotifications(freshStreams, deadStreams);
                            jobFinished(params, false);
                        },
                        error -> {
                            Log.e(TAG, "Cant execute notification job", error);
                            jobFinished(params, true);
                        });
        Log.d(TAG, "onStartJob: finish job");
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "onStopJob: dispose resources");
        if (mRetrieveLiveStreams != null) {
            mRetrieveLiveStreams.dispose();
        }
        if (mRetrieveAvatarBitmaps != null) {
            mRetrieveAvatarBitmaps.dispose();
        }
        return false;
    }

    private List<Stream> compareAndGetDeadStreams(List<Stream> oldStreams,
                                                  List<Stream> newStreams) {
        List<Stream> outList = new ArrayList<>();
        for (Stream newElement : oldStreams) {
            if (!newStreams.contains(newElement)) {
                outList.add(newElement);
            }
        }
        return outList;
    }


    private List<Stream> getLastLiveStreams() {
        List<Stream> streams = mRepository.getLastStreamList();
        for (Stream element : streams) {
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
        for (Stream newElement : newStreams) {
            if (!oldStreams.contains(newElement)) {
                outList.add(newElement);
            }
        }
        return outList;
    }

    private void constructNotifications(List<Stream> freshStreams, List<Stream> deadStreams) {
        if (freshStreams.isEmpty()) {
            Log.d(TAG, "constructNotifications: no fresh streams -> no notifications");
            return;
        }

        NotificationManager notificationManager = (NotificationManager)
                getApplicationContext().getSystemService(NOTIFICATION_SERVICE);

        //hide all dead notifications
        for (Stream deadStream : deadStreams) {
            notificationManager.cancel(deadStream.getUserId().hashCode());
        }

        //construct all new notifications
        HashMap<String, NotificationCompat.Builder> newNotifications = new HashMap<>();

        for (Stream freshStream : freshStreams) {

            UserData streamingUser = freshStream.getUserData();
            PendingIntent intent = constructStreamIntent(freshStream);

            NotificationCompat.Builder notification = new NotificationCompat.Builder(mContext,
                    getString(R.string.live_streamer_notification_id))
                    .setAutoCancel(true)
                    .setContentIntent(intent)
                    .setContentTitle(freshStream.getStreamerName())
                    .setContentText(freshStream.getTitle())
                    .setSubText(freshStream.getViewerCount() + " viewers")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setGroup(NOTIFICATION_GROUP_KEY);
            newNotifications.put(streamingUser.getId(), notification);
        }

        Notification summaryNotification = new NotificationCompat.Builder(mContext,
                getString(R.string.live_streamer_notification_id))
                .setAutoCancel(true)
                .setContentIntent(constructMainIntent())
                .setContentTitle(freshStreams.size() + " streams went live") // todo res
                .setSmallIcon(R.mipmap.ic_launcher)
                .setGroup(NOTIFICATION_GROUP_KEY)
                .setGroupSummary(true)
                .build();

        //fulfill profile images and fire notifications, asynchronously

        mRetrieveAvatarBitmaps = getAvatarsForStreamlist(freshStreams)
                .subscribe(
                        stringBitmapMap -> {
                            for (String key: stringBitmapMap.keySet()) {
                                Bitmap avatar = stringBitmapMap.get(key);
                                NotificationCompat.Builder builder = newNotifications.get(key);
                                if (builder != null) {
                                    builder.setLargeIcon(avatar);
                                    Notification notification = builder.build();
                                    notificationManager.notify(key.hashCode(), notification);
                                }
                            }
                            notificationManager.notify(NOTIFICATION_SUMMARY, summaryNotification);
                        },
                        error -> Log.e(TAG, "constructNotifications: error", error));

    }


    private Single<Map<String, Bitmap>> getAvatarsForStreamlist(List<Stream> streamList) {
        return Observable
                .fromIterable(streamList)
                .map(Stream::getUserData)
                .observeOn(mSchedulersProvider.getIoScheduler())
                .map(userData -> new Pair<>(userData.getId(),
                        mPicasso.load(userData.getProfileImageUrl()).get()))
                .toMap(userDataBitmapPair -> userDataBitmapPair.first,
                        userDataBitmapPair -> userDataBitmapPair.second)
                .subscribeOn(mSchedulersProvider.getUiScheduler());
    }

    private PendingIntent constructStreamIntent(Stream stream) {
        Intent intent = new Intent(mContext, StreamChatActivity.class);
        intent.putExtra("stream_json", mGson.toJson(stream)); //todo strings
        return PendingIntent.getActivity(mContext, stream.getUserId().hashCode(), intent, 0);
    }

    private PendingIntent constructMainIntent() {
        Intent intent = new Intent(mContext, StreamListActivity.class);
        return PendingIntent.getActivity(mContext, 0, intent, 0);
    }
}
