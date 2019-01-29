package ru.nubby.playstream.utils;

import android.content.Context;
import android.content.res.Resources;

import ru.nubby.playstream.R;

public enum Quality {
    QUALITY108060,
    QUALITY108030,
    QUALITY72060,
    QUALITY72030,
    QUALITY480,
    QUALITY360,
    QUALITY240,
    QUALITY_AUDIO_ONLY,
    DEFAULT;

    public String getQualityShortName(Context context) {
        Resources res = context.getResources();
        String[] names = res.getStringArray(R.array.quality_list);
        return names[this.ordinal()];
    }
}
