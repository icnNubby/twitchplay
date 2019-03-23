package ru.nubby.playstream.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * <a href = "https://dev.twitch.tv/docs/api/reference/#get-streams">
 * Query parameters here.</a>
 */
public class StreamsRequest {

    /**
     * Contains an array of {@link Stream} information elements.
     */
    @SerializedName("data")
    private List<Stream> data;

    /**
    * Contains information, required to query for more streams.
     */
    @SerializedName("pagination")
    private Pagination pagination;

    public List<Stream> getData() {
        return data;
    }

    public void setData(List<Stream> streams) {
        this.data = streams;
    }

    public Pagination getPagination() {
        return pagination;
    }
}
