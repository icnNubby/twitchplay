package ru.nubby.playstream.domain.entities;

import android.content.Context;
import android.content.res.Resources;

import ru.nubby.playstream.R;

/**
* Enumerated list of currently supported qualities. Default is the original quality.
 */
public enum Quality {
    DEFAULT,
    QUALITY108060,
    QUALITY108030,
    QUALITY72060,
    QUALITY72030,
    QUALITY480,
    QUALITY360,
    QUALITY240,
    QUALITY_AUDIO_ONLY;

    public String getQualityShortName(Context context) {
        Resources res = context.getResources();
        String[] names = res.getStringArray(R.array.quality_list);
        return names[this.ordinal()];
    }
}
