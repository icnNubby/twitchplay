package ru.nubby.playstream.domain.entities;

import com.google.gson.annotations.SerializedName;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(tableName = "follow_relations", primaryKeys = {"from_id", "to_id"})
public final class FollowRelations {
    @ColumnInfo(name = "from_id")
    @SerializedName("from_id")
    @NonNull
    private String fromId;

    @ColumnInfo(name = "from_name")
    @SerializedName("from_name")
    private String fromName;

    @ColumnInfo(name = "to_id")
    @SerializedName("to_id")
    @NonNull
    private String toId;

    @ColumnInfo(name = "to_name")
    @SerializedName("to_name")
    private String toName;

    @ColumnInfo(name = "followed_at")
    @SerializedName("followed_at")
    private String followedAt;

    public FollowRelations(@NonNull String fromId, String fromName, @NonNull String toId,
                           String toName, String followedAt) {
        this.fromId = fromId;
        this.fromName = fromName;
        this.toId = toId;
        this.toName = toName;
        this.followedAt = followedAt;
    }

    @NonNull
    public String getFromId() {
        return fromId;
    }

    public void setFromId(@NonNull String fromId) {
        this.fromId = fromId;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    @NonNull
    public String getToId() {
        return toId;
    }

    public void setToId(@NonNull String toId) {
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

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof FollowRelations)) return false;
        return ((FollowRelations) obj).fromId.equals(this.fromId) &&
                ((FollowRelations) obj).toId.equals(this.toId);
    }

    @Override
    public int hashCode() {
        return this.toId.hashCode() * 31 + this.fromId.hashCode();
    }
}
