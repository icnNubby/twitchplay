package ru.nubby.playstream.domain.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class QualityLinks {
    private final Stream stream;
    private final Map<Quality, String> qualityMap;

    public QualityLinks(@NonNull Stream stream,
                        @NonNull Map<Quality, String> qualityMap) {
        this.stream = stream;
        this.qualityMap = qualityMap;
    }


    public Stream getStream() {
        return stream;
    }

    /**
     * Returns url for chosen quality if it has that.
     * Searches for higher quality if no url for given quality is found.
     * Returns null, if no higher quality is available.
     * @param quality quality to search.
     * @return String url of HLS resource for given quality.
     */
    @Nullable
    public String getUrlForQualityOrClosest(Quality quality) {

        String url = qualityMap.get(quality);
        if (url != null) {
            return url;
        }
        Quality nextQuality = quality;
        while (url == null && nextQuality.ordinal() >= 0) {
            nextQuality = Quality.values()[nextQuality.ordinal() - 1];
            url = qualityMap.get(nextQuality);
        }

        if (url != null) {
            return url;
        } else {
            return null;
        }
    }

    public List<Quality> getSortedQualities() {
        List<Quality> qualities = new ArrayList<>(qualityMap.keySet());
        Collections.sort(qualities);
        return qualities;
    }
}
