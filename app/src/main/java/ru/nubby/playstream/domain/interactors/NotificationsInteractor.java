package ru.nubby.playstream.domain.interactors;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Single;
import ru.nubby.playstream.data.Repository;
import ru.nubby.playstream.data.sharedprefs.DefaultPreferences;
import ru.nubby.playstream.data.sharedprefs.PersistentStorage;
import ru.nubby.playstream.domain.entities.Stream;
import ru.nubby.playstream.presentation.preferences.utils.TimePreference;

/**
 * Business logic for notifications.
 */
@Singleton
public class NotificationsInteractor {

    private final PersistentStorage mPersistentStorage;
    private final DefaultPreferences mDefaultPreferences;
    private final AuthInteractor mAuthInteractor;
    private final Repository mRepository;

    @Inject
    public NotificationsInteractor(PersistentStorage persistentStorage,
                                   DefaultPreferences defaultPreferences,
                                   AuthInteractor authInteractor,
                                   Repository repository) {
        mPersistentStorage = persistentStorage;
        mDefaultPreferences = defaultPreferences;
        mRepository = repository;
        mAuthInteractor = authInteractor;
    }

    /**
     * Checks if all the preconditions, setted up in preferences, are true.
     * Ex. notifications are on, user is logged and current time is not in silent zone.
     * @return can we fire notifications or not.
     */
    public boolean conditionsSatisfied() {

        if (mAuthInteractor.getCurrentLoggedStatus() != AuthInteractor.LoggedStatus.LOGGED) {
            return false;
        }

        if (!mDefaultPreferences.getNotificationsAreOn()) {
            return false;
        }

        boolean silent = false;

        if (mDefaultPreferences.getSilentHoursAreOn()) {
            if (mDefaultPreferences.getSilentHoursStartTime()
                    .equals(mDefaultPreferences.getSilentHoursFinishTime())) {
                return true;
            }

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());

            int startHour = TimePreference.parseHour(mDefaultPreferences.getSilentHoursStartTime());
            int startMinute = TimePreference.parseMinute(mDefaultPreferences.getSilentHoursStartTime());
            int startTimeTotal = startHour * 60 + startMinute;

            int endHour = TimePreference.parseHour(mDefaultPreferences.getSilentHoursFinishTime());
            int endMinute = TimePreference.parseMinute(mDefaultPreferences.getSilentHoursFinishTime());
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

    /**
     * Proxy method to retrieve current live streams from remote repo.
     * @return
     */
    public Single<List<Stream>> getLiveStreams() {
        return mRepository.getLiveStreamsFollowedByUser();
    }

    /**
     * Compares two stream lists and returns list of streams that came offline from last retrieve.
     * @param oldStreams list of previously retrieved streams
     * @param newStreams list of streams currently live
     * @return dead streams list.
     */
    public List<Stream> compareAndGetDeadStreams(List<Stream> oldStreams,
                                                 List<Stream> newStreams) {
        List<Stream> outList = new ArrayList<>();
        for (Stream newElement : oldStreams) {
            if (!newStreams.contains(newElement)) {
                outList.add(newElement);
            }
        }
        return outList;
    }

    /**
     * Gets last retrieved stream list.
     * This stream list is saved in {@link ru.nubby.playstream.data.sharedprefs.PersistentStorage}.
     */
    public List<Stream> getLastLiveStreams() {
        return mPersistentStorage.getStreamList();
    }

    /**
     * Compares two stream lists and returns list of streams that came online from last retrieve.
     * @param oldStreams list of previously retrieved streams
     * @param newStreams list of streams currently live
     * @return fresh streams list.
     */
    public List<Stream> compareAndGetFreshStreams(List<Stream> oldStreams,
                                                  List<Stream> newStreams) {
        List<Stream> outList = new ArrayList<>();
        for (Stream newElement : newStreams) {
            if (!oldStreams.contains(newElement)) {
                outList.add(newElement);
            }
        }
        return outList;
    }



}
