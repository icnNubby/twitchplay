package ru.nubby.playstream.domain.interactors;

import javax.inject.Inject;
import javax.inject.Singleton;

import ru.nubby.playstream.data.sources.sharedprefs.DefaultPreferences;
import ru.nubby.playstream.domain.entities.Quality;

/**
 * Business logic for retrieving preferences.
 * Note, that we do not set preferences here, framework handles it for us, but it is possible.
 */
@Singleton
public class PreferencesInteractor {

    private final DefaultPreferences mDefaultPreferences;

    @Inject
    public PreferencesInteractor(DefaultPreferences defaultPreferences) {
        mDefaultPreferences = defaultPreferences;
    }

    public Quality getDefaultQuality() {
        return mDefaultPreferences.getDefaultQuality();
    }

    public int getPreviewSize() {
        return mDefaultPreferences.getPreviewSize();
    }

    public boolean getNotificationsAreOn() {
        return mDefaultPreferences.getNotificationsAreOn();
    }

    public boolean getSilentHoursAreOn() {
        return mDefaultPreferences.getSilentHoursAreOn();
    }

    public String getSilentHoursStartTime() {
        return mDefaultPreferences.getSilentHoursStartTime();
    }

    public String getSilentHoursFinishTime() {
        return mDefaultPreferences.getSilentHoursFinishTime();
    }
}
