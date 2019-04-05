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

import java.util.ArrayList;
import java.util.Calendar;
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
import ru.nubby.playstream.data.sharedprefs.DefaultPreferences;
import ru.nubby.playstream.domain.entity.Stream;
import ru.nubby.playstream.domain.entity.UserData;
import ru.nubby.playstream.domain.interactor.PreferencesInteractor;
import ru.nubby.playstream.presentation.preferences.utils.TimePreference;
import ru.nubby.playstream.presentation.stream.StreamChatActivity;
import ru.nubby.playstream.presentation.streamlist.StreamListActivity;
import ru.nubby.playstream.utils.Constants;
import ru.nubby.playstream.utils.RxSchedulersProvider;

public class NotificationService extends JobService {

    private static final String TAG = NotificationService.class.getSimpleName();

    private static final String NOTIFICATION_GROUP_KEY = "ru.nubby.playstream.NEW_LIVE_STREAMS";

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

    @Inject
    PreferencesInteractor mPreferencesInteractor;

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

        if (!conditionsSatisfied()) {
            jobFinished(params, false);
            return true;
        }

        final List<Stream> lastStreams = getLastLiveStreams();

        mRetrieveLiveStreams = getLiveStreams()
                .filter(streamList -> !streamList.isEmpty())
                .subscribeOn(mSchedulersProvider.getIoScheduler())
                .observeOn(mSchedulersProvider.getUiScheduler())
                .subscribe(
                        streams -> {
                            List<Stream> freshStreams =
                                    compareAndGetFreshStreams(lastStreams, streams);
                            List<Stream> deadStreams =
                                    compareAndGetDeadStreams(lastStreams, streams);

                            constructNotifications(freshStreams, deadStreams);
                            jobFinished(params, false);
                        },
                        error -> {
                            jobFinished(params, true);
                        });
        return true;
    }

    private boolean conditionsSatisfied() {

        if (!mPreferencesInteractor.getNotificationsAreOn()) {
            return false;
        }

        boolean silent = false;

        if (mPreferencesInteractor.getSilentHoursAreOn()) {
            if (mPreferencesInteractor.getSilentHoursStartTime()
                    .equals(mPreferencesInteractor.getSilentHoursFinishTime())) {
                return true;
            }

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());

            int startHour = TimePreference.parseHour(mPreferencesInteractor.getSilentHoursStartTime());
            int startMinute = TimePreference.parseMinute(mPreferencesInteractor.getSilentHoursStartTime());
            int startTimeTotal = startHour * 60 + startMinute;

            int endHour = TimePreference.parseHour(mPreferencesInteractor.getSilentHoursFinishTime());
            int endMinute = TimePreference.parseMinute(mPreferencesInteractor.getSilentHoursFinishTime());
            int endTimeTotal = endHour * 60 + endMinute;

            int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
            int currentMinute = calendar.get(Calendar.MINUTE);
            int currentTimeTotal = currentHour * 60 + currentMinute;

            //start time is at same day as end time
            if (startTimeTotal < endTimeTotal &&
                    currentTimeTotal >= startTimeTotal &&
                    currentTimeTotal <= endTimeTotal) {
                silent = true;
            }
            //start time is at another day than end time
            if (startTimeTotal > endTimeTotal &&
                    (currentTimeTotal > startTimeTotal ||
                    currentTimeTotal < endTimeTotal)) {
                silent = true;
            }
        }
        return !silent;
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
