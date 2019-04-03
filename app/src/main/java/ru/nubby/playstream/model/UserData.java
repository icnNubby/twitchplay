package ru.nubby.playstream.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 *
 * <a href = "https://dev.twitch.tv/docs/api/reference/#get-users">User data.</a>
 */
@Entity (tableName = "user_data")
public class UserData implements Serializable {

    @PrimaryKey
    @SerializedName("id")
    @NonNull
    private String id = "";
    @SerializedName("login")
    @NonNull
    private String login = "";
    @SerializedName("display_name")
    private String displayName;
    @SerializedName("type")
    private String type;
    @SerializedName("broadcaster_type")
    private String broadcasterType;
    @SerializedName("description")
    private String description;
    @SerializedName("profile_image_url")
    private String profileImageUrl;
    @SerializedName("offline_image_url")
    private String offlineImageUrl;
    @SerializedName("view_count")
    private Integer viewCount;
    @SerializedName("email")
    private String email;

    public UserData(){

    }

    public UserData(UserData userData){
        this.id = userData.getId();
        this.login = userData.getLogin();
        this.displayName = userData.getDisplayName();
        this.type = userData.getType();
        this.broadcasterType = userData.getBroadcasterType();
        this.description = userData.getDescription();
        this.profileImageUrl = userData.getProfileImageUrl();
        this.offlineImageUrl = userData.getOfflineImageUrl();
        this.viewCount = userData.getViewCount();
        this.email = userData.getEmail();
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    @NonNull
    public String getLogin() {
        return login;
    }

    public void setLogin(@NonNull String login) {
        this.login = login;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBroadcasterType() {
        return broadcasterType;
    }

    public void setBroadcasterType(String broadcasterType) {
        this.broadcasterType = broadcasterType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getOfflineImageUrl() {
        return offlineImageUrl;
    }

    public void setOfflineImageUrl(String offlineImageUrl) {
        this.offlineImageUrl = offlineImageUrl;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEmpty(){
        return this.id.isEmpty() && this.login.isEmpty();
    }

}
