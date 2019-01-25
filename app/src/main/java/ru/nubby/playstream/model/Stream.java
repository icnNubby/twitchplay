package ru.nubby.playstream.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Stream { //TODO implement all models + GSON convertation obv

    @SerializedName("title")
    @Expose
    private String title;

    @SerializedName("user_name")
    @Expose
    private String streamer_name;


    private String description;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStreamer_name() {
        return streamer_name;
    }

    public void setStreamer_name(String streamer_name) {
        this.streamer_name = streamer_name;
    }
}
