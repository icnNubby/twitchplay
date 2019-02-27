package ru.nubby.playstream.model;

import com.google.gson.annotations.SerializedName;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Stream class. Contains information about user, stream title, viewer count, etc.
 * <a href = "https://dev.twitch.tv/docs/api/reference/#get-streams">Documentation here.</a>
 */
public class Stream implements Comparable<Stream> {

    @SerializedName("title")
    private String title;

    @NonNull
    @SerializedName("user_id")
    private String userId;

    @SerializedName("user_name")
    private String streamerName;

    /**
     * This is always in latin letters(english alphabet), should be used in all queries instead of
     * invalid user_name fields in chinese symbols
     */
    @SerializedName("user_login")
    private String streamerLogin;

    /**
     * Stream type: "live" or "" (in case of error).
     */
    @SerializedName("type")
    private String type;

    @SerializedName("viewer_count")
    private String viewerCount;

    @SerializedName("thumbnail_url")
    private String thumbnailUrl;

    private UserData userData;

    public String getTitle() {
        return title;
    }

    public String getStreamerName() {
        return streamerName;
    }

    public String getType() {
        return type;
    }

    @NonNull
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

    @Override
    public int compareTo(Stream o) throws NumberFormatException {
        return Integer.valueOf(o.getViewerCount()) - Integer.valueOf(this.getViewerCount());
    }

    public UserData getUserData() {
        return userData;
    }

    public void setUserData(UserData userData) throws IllegalArgumentException {
        if (userData.getId().equals(userId)) {
            this.userData = userData;
        } else {
            throw new IllegalArgumentException("Id's of userData and Stream objects must be the same.");
        }
    }

    @Override
    public int hashCode() {
        return userId.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof Stream)) {
            return false;
        }
        return this.userId.equals(((Stream) obj).userId);
    }
}
