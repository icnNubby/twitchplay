package ru.nubby.playstream.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FollowRelations {

    @SerializedName("from_id")
    @Expose
    private String fromId;
    @SerializedName("from_name")
    @Expose
    private String fromName;
    @SerializedName("to_id")
    @Expose
    private String toId;
    @SerializedName("to_name")
    @Expose
    private String toName;
    @SerializedName("followed_at")
    @Expose
    private String followedAt;

    public String getFromId() {
        return fromId;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getToId() {
        return toId;
    }

    public void setToId(String toId) {
        this.toId = toId;
    }

    public String getToName() {
        return toName;
    }

    public void setToName(String toName) {
        this.toName = toName;
    }

    public String getFollowedAt() {
        return followedAt;
    }

    public void setFollowedAt(String followedAt) {
        this.followedAt = followedAt;
    }
}
