package ru.nubby.playstream.domain.sharedprefs;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;
import ru.nubby.playstream.R;
import ru.nubby.playstream.model.Quality;

public class DefaultPreferences {

    private SharedPreferences mSharedPreferences;
    private Context mContext; //BEWARE of leaks

    public DefaultPreferences(Context context) {
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
}
