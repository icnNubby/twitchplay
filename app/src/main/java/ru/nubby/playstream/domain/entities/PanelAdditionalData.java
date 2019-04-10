package ru.nubby.playstream.domain.entities;

import com.google.gson.annotations.SerializedName;

/**
 * Addition data for {@link ChannelPanel} class.
 * Just following backend api.
 */
public final class PanelAdditionalData {

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

    public void setLink(String link) {
        this.link = link;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

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
}
