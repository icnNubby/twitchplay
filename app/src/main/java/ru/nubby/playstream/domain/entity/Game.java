package ru.nubby.playstream.domain.entity;

import com.google.gson.annotations.SerializedName;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "games")
public final class Game {

    @PrimaryKey
    @ColumnInfo(name = "id")
    @SerializedName("id")
    private String id;

    @ColumnInfo(name = "name")
    @SerializedName("name")
    private String name;

    @ColumnInfo(name = "box_art_url")
    @SerializedName("box_art_url")
    private String boxArtUrl;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBoxArtUrl() {
        return boxArtUrl;
    }

    public void setBoxArtUrl(String boxArtUrl) {
        this.boxArtUrl = boxArtUrl;
    }

}
