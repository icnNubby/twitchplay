package ru.nubby.playstream.domain.entities;

import android.content.Context;
import android.content.res.Resources;

import ru.nubby.playstream.R;

/**
 * Enumerated list of currently supported qualities.
 * Default is the original quality.
 */
public enum Quality {
    //IMPORTANT, IF U CHANGE ORDER HERE - CHANGE ORDER IN R.array.quality_list!
    DEFAULT,
    QUALITY108060,
    QUALITY108030,
    QUALITY72060,
    QUALITY72030,
    QUALITY480,
    QUALITY360,
    QUALITY240,
    QUALITY_AUDIO_ONLY;

    /**
     * Uses string resource array to store names for qualities.
     * Resources and enum are bound by position index, be aware of it.
     * @param context android context
     * @return (translated) representation of enum
     */
    public String getQualityShortName(Context context) {
        Resources res = context.getResources();
        String[] names = res.getStringArray(R.array.quality_list);
        return names[this.ordinal()];
    }
}
