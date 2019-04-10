package ru.nubby.playstream.domain.entities;

import com.google.gson.annotations.SerializedName;

/**
 * Undocumented feature of old api.
 * <a href = "https://api.twitch.tv/api/channels/gn_gg/panels">Some panels under the channel.</a>
 */
public final class ChannelPanel {

    @SerializedName("_id")
    private Integer id;

    @SerializedName("user_id")
    private Integer userId;

    @SerializedName("display_order")
    private Integer displayOrder;

    @SerializedName("kind")
    private String kind;

    @SerializedName("data")
    private PanelAdditionalData data;

    @SerializedName("html_description")
    private String htmlDescription;

    @SerializedName("channel")
    private String channel;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public PanelAdditionalData getData() {
        return data;
    }

    public void setData(PanelAdditionalData data) {
        this.data = data;
    }

    public String getHtmlDescription() {
        return htmlDescription;
    }

    public void setHtmlDescription(String htmlDescription) {
        this.htmlDescription = htmlDescription;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

}
