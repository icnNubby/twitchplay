package ru.nubby.playstream.domain.entities;

import com.google.gson.annotations.SerializedName;

/**
 * Addition data for {@link ChannelPanel} class.
 * Just following backend api.
 */
public final class ChannelPanelAdditionalData {

    @SerializedName("link")
    private String link;

    @SerializedName("image")
    private String image;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    public String getLink() {
        return link;
    }

    public String getImage() {
        return image;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}
