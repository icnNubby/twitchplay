package ru.nubby.playstream.domain.entities;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Stream class. Contains information about user, stream title, viewer count, etc.
 * <p></p>
 * Comes incomplete. userData and game fields have to be fetched with additional requests.
 * <a href = "https://dev.twitch.tv/docs/api/reference/#get-streams">Documentation here.</a>
 */
public final class Stream implements Comparable<Stream>, Serializable {

    @SerializedName("title")
    private String title;

    @NonNull
    @SerializedName("user_id")
    private String userId = "";

    @SerializedName("user_name")
    private String streamerName;

    @SerializedName("game_id")
    private String gameId;

    /**
     * This is always in latin letters(english alphabet), should be used in some queries instead of
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

    private Game game;

    public Stream() {

    }

    /**
     * Copy constructor.
     * Also copies game and userData fields.
     * @param stream stream to copy
     */
    public Stream(@NonNull Stream stream) {
        this.userId = stream.getUserId();
        this.gameId = stream.getGameId();
        this.streamerName = stream.getStreamerName();
        this.streamerLogin = stream.getStreamerLogin();
        this.title = stream.getTitle();
        this.type = stream.getType();
        this.viewerCount = stream.getViewerCount();
        this.thumbnailUrl = stream.getThumbnailUrl();
        if (stream.getUserData() != null) {
            this.userData = new UserData(stream.getUserData());
        } else {
            this.userData = null;
        }

        if (stream.getGame() != null) {
            this.game = new Game(stream.getGame());
        } else {
            this.game = null;
        }

    }

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

    public void setUserData(@NonNull UserData userData) {
        this.userData = userData;
        this.userId = userData.getId();
        this.streamerLogin = userData.getLogin();
        this.streamerName = userData.getDisplayName();
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

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public void setGame(@NonNull Game game) {
        this.game = game;
        this.gameId = game.getId();
    }

    public Game getGame() {
        return game;
    }
}
