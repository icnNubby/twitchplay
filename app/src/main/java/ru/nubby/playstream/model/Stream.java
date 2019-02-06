package ru.nubby.playstream.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Stream class. Contains information about user, stream title, viewer count, etc.
 * <a href = "https://dev.twitch.tv/docs/api/reference/#get-streams">Documentation here.</a>
 */
public class Stream {

    @SerializedName("title")
    @Expose
    private String title;

    @SerializedName("user_id")
    @Expose
    private String userId;

    @SerializedName("user_name")
    @Expose
    private String streamerName; //TODO deal with chinese streams

    /**
     * This is always in latin letters(english alphabet), should be used in all queries instead of
     * invalid user_name fields in chinese symbols
     */
    @SerializedName("user_login")
    @Expose
    private String streamerLogin;

    /**
     * Stream type: "live" or "" (in case of error).
     */
    @SerializedName("type")
    @Expose
    private String type;

    @SerializedName("viewer_count")
    @Expose
    private String viewerCount;

    @SerializedName("thumbnail_url")
    @Expose
    private String thumbnailUrl;

    public String getTitle() {
        return title;
    }

    public String getStreamerName() {
        return streamerName;
    }

    public String getType() {
        return type;
    }

    public String getUserId() {
        return userId;
    }

    public String getViewerCount() {
        return viewerCount;
    }

    public String getStreamerLogin() {
        return streamerLogin;
    }

    public void setStreamerLogin(String streamerLogin) {
        this.streamerLogin = streamerLogin;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
}
