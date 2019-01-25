package ru.nubby.playstream.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GsonScheme {

    @SerializedName("data")
    @Expose
    private List<Stream> data;

    public List<Stream> getData() {
        return data;
    }

    public void setData(List<Stream> data) {
        this.data = data;
    }

    //todo pagination https://dev.twitch.tv/docs/api/reference/#get-streams
}
