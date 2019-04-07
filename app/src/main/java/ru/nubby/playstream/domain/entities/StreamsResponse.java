package ru.nubby.playstream.domain.entities;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import ru.nubby.playstream.domain.StreamsRepository;

/**
 * <a href = "https://dev.twitch.tv/docs/api/reference/#get-streams">
 * Query parameters here.</a>
 */
public final class StreamsResponse {

    /**
     * Contains list of {@link Stream} information elements.
     */
    @SerializedName("data")
    private List<Stream> data;

    /**
    * Contains information, required to query for more streams.
     */
    @SerializedName("pagination")
    private Pagination pagination;

    public StreamsResponse() {

    }

    public StreamsResponse(StreamsResponse streamsResponse) {
        this.data = new ArrayList<>();
        for (Stream stream: streamsResponse.getData()) {
            this.data.add(new Stream(stream));
        }
        this.pagination = new Pagination(streamsResponse.getPagination());
    }

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
