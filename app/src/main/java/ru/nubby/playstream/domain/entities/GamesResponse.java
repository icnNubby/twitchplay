package ru.nubby.playstream.domain.entities;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public final class GamesResponse {

    @SerializedName("data")
    private List<Game> data = null;

    /**
     * Contains information, required to query for more streams.
     */
    @SerializedName("pagination")
    private Pagination pagination;

    public List<Game> getData() {
        return data;
    }

    public void setData(List<Game> data) {
        this.data = data;
    }

    public Pagination getPagination() {
        return pagination;
    }
}
