package ru.nubby.playstream.domain.entities;

import com.google.gson.annotations.SerializedName;

import androidx.annotation.Nullable;

/**
 * Undocumented feature of old api.
 * <a href = "https://api.twitch.tv/api/channels/gn_gg/panels">Some panels under the channel.</a>
 */
public final class ChannelPanel implements Comparable<ChannelPanel> {

    @SerializedName("_id")
    private Integer id;

    @SerializedName("user_id")
    private Integer userId;

    @SerializedName("display_order")
    private Integer displayOrder;

    @SerializedName("kind")
    private String kind;

    @SerializedName("data")
    private ChannelPanelAdditionalData data;

    @SerializedName("html_description")
    private String htmlDescription;

    @SerializedName("channel")
    private String channel;

    public Integer getId() {
        return id;
    }

    public Integer getUserId() {
        return userId;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public String getKind() {
        return kind;
    }

    public ChannelPanelAdditionalData getData() {
        return data;
    }

    public String getHtmlDescription() {
        return htmlDescription;
    }

    public String getChannel() {
        return channel;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof ChannelPanel)) {
            return false;
        }
        return (((ChannelPanel) obj).userId.equals(this.id));
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public int compareTo(ChannelPanel o) {
        return this.displayOrder.compareTo(o.displayOrder);
    }
}
