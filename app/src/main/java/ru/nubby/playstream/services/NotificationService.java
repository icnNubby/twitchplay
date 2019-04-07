package ru.nubby.playstream.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.util.Log;
import android.util.Pair;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

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
import ru.nubby.playstream.domain.entities.Stream;
import ru.nubby.playstream.domain.entities.UserData;
import ru.nubby.playstream.domain.interactors.NotificationsInteractor;
import ru.nubby.playstream.presentation.stream.StreamChatActivity;
import ru.nubby.playstream.presentation.streamlist.StreamListActivity;
import ru.nubby.playstream.utils.Constants;
import ru.nubby.playstream.utils.RxSchedulersProvider;

public class NotificationService extends JobService {

    private static final String TAG = NotificationService.class.getSimpleName();

    private static final String NOTIFICATION_GROUP_KEY = "ru.nubby.playstream.NEW_LIVE_STREAMS";

    private static final int NOTIFICATION_SUMMARY = 112112;

    @Inject
    RxSchedulersProvider mSchedulersProvider;

    @Inject
    Context mContext;

    @Inject
    Gson mGson;

    @Inject
    NotificationsInteractor mNotificationsInteractor;

    private Picasso mPicasso;

    private Disposable mRetrieveLiveStreams;
    private Disposable mRetrieveAvatarBitmaps;

    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
        super.onCreate();
        mPicasso = Picasso.get();
    }

    @Override
    public boolean onStartJob(JobParameters params) {

        if (!mNotificationsInteractor.conditionsSatisfied()) {
            jobFinished(params, false);
            return true;
        }

        final List<Stream> lastStreams = mNotificationsInteractor.getLastLiveStreams();

        mRetrieveLiveStreams = mNotificationsInteractor
                .getLiveStreams()
                .filter(streamList -> !streamList.isEmpty())
                .observeOn(mSchedulersProvider.getUiScheduler())
                .subscribe(
                        streams -> {
                            List<Stream> freshStreams =
                                    mNotificationsInteractor.compareAndGetFreshStreams(lastStreams,
                                            streams);
                            List<Stream> deadStreams =
                                    mNotificationsInteractor.compareAndGetDeadStreams(lastStreams,
                                            streams);

                            constructNotifications(freshStreams, deadStreams);
                            jobFinished(params, false);
                        },
                        error -> {
                            jobFinished(params, true);
                        });
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        if (mRetrieveLiveStreams != null) {
            mRetrieveLiveStreams.dispose();
        }
        if (mRetrieveAvatarBitmaps != null) {
            mRetrieveAvatarBitmaps.dispose();
        }
        return false;
    }

    private void constructNotifications(List<Stream> freshStreams, List<Stream> deadStreams) {
        if (freshStreams.isEmpty()) {
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
                    .setDefaults(0)
                    .setLights(Color.BLUE, 5000, 5000)
                    .setVibrate(new long[] {0,100,0,0})
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setContentTitle(freshStream.getStreamerName())
                    .setContentText(freshStream.getTitle())
                    .setSubText(mContext.getString(R.string.notification_stream_viewers,
                            freshStream.getViewerCount()))
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setGroup(NOTIFICATION_GROUP_KEY);
            newNotifications.put(streamingUser.getId(), notification);
        }

        Notification summaryNotification = new NotificationCompat.Builder(mContext,
                getString(R.string.live_streamer_notification_id))
                .setAutoCancel(true)
                .setDefaults(0)
                .setLights(Color.BLUE, 5000, 5000)
                .setVibrate(new long[] {0,100,0,0})
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(constructMainIntent())
                .setContentTitle(mContext.getString(R.string.notification_streams_live,
                        newNotifications.size()))
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setGroup(NOTIFICATION_GROUP_KEY)
                .setGroupSummary(true)
                .build();

        //fulfill profile images and fire notifications after, asynchronously
        mRetrieveAvatarBitmaps = getAvatarsForStreamlist(freshStreams)
                .subscribe(
                        stringBitmapMap -> {
                            for (String key : stringBitmapMap.keySet()) {
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

        int requestCode = stream.getUserId().hashCode();
        Intent intent = new Intent(mContext, StreamChatActivity.class);
        intent.putExtra(Constants.sStreamIntentKey, mGson.toJson(stream));

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        stackBuilder.addParentStack(StreamChatActivity.class);
        stackBuilder.addNextIntent(intent);

        return stackBuilder.getPendingIntent(requestCode, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent constructMainIntent() {
        Intent intent = new Intent(mContext, StreamListActivity.class);
        return PendingIntent.getActivity(mContext, 0, intent, 0);
    }
}
