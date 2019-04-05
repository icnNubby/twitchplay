package ru.nubby.playstream.domain.entities;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public final class GamesResponse {

    @SerializedName("data")
    @Expose
    private List<Game> data = null;

    public List<Game> getData() {
        return data;
    }

    public void setData(List<Game> data) {
        this.data = data;
    }
}
