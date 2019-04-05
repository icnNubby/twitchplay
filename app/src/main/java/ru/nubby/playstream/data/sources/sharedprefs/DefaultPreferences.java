package ru.nubby.playstream.data.sources.sharedprefs;

import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Inject;
import javax.inject.Singleton;

import androidx.preference.PreferenceManager;
import ru.nubby.playstream.R;
import ru.nubby.playstream.domain.entities.Quality;

@Singleton
public class DefaultPreferences {

    private SharedPreferences mSharedPreferences;
    private Context mContext;

    @Inject
    DefaultPreferences(Context context) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mContext = context;
    }

    public int getDefaultStreamListMode() {
        String value = mSharedPreferences.getString(
                mContext.getString(R.string.default_stream_list_mode_key),
                mContext.getString(R.string.default_stream_list_mode_default_value));
        if (value != null) {
            return Integer.valueOf(value);
        } else {
            return 0;
        }

    }

    public Quality getDefaultQuality() {
        String qualityString = mSharedPreferences.getString(
                mContext.getString(R.string.default_quality_key),
                mContext.getString(R.string.default_quality_default_value));
        String[] qualityList = mContext.getResources().getStringArray(R.array.quality_list);

        int index = 0;
        for (int i = 0; i < qualityList.length; i++) {
            if (qualityList[i].equals(qualityString)) {
                index = i;
                break;
            }
        }
        return Quality.values()[index];
    }

    public int getPreviewSize() {
        String value = mSharedPreferences.getString(
                mContext.getString(R.string.stream_list_item_size_key),
                mContext.getString(R.string.stream_list_item_size_default_value));
        if (value != null) {
            return Integer.valueOf(value);
        } else {
        return 0;
    }
}

    public boolean getNotificationsAreOn() {
        return mSharedPreferences.getBoolean(
                mContext.getString(R.string.notifications_are_on_key),
                mContext.getResources().getBoolean(R.bool.default_notifications_are_on));
    }

    public boolean getSilentHoursAreOn() {
        return mSharedPreferences.getBoolean(
                mContext.getString(R.string.silent_hours_key),
                mContext.getResources().getBoolean(R.bool.default_silent_hours_are_on));
    }

    public String getSilentHoursStartTime() {
        return mSharedPreferences.getString(
                mContext.getString(R.string.silent_time_start_key),
                mContext.getString(R.string.silent_time_start_default_value));
    }

    public String getSilentHoursFinishTime() {
        return mSharedPreferences.getString(
                mContext.getString(R.string.silent_time_finish_key),
                mContext.getString(R.string.silent_time_finish_default_value));
    }
}
