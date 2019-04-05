package ru.nubby.playstream.domain.entities;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * <a href = "https://dev.twitch.tv/docs/api/reference/#get-users-follows">
 * Query parameters here.</a>
 */
public final class UserFollowsResponse {

    @SerializedName("total")
    private Integer total;
    /**
     * Contains an array of {@link FollowRelations} information elements.
     */
    @SerializedName("data")
    private List<FollowRelations> data;

    /**
     * Contains information, required to query for more follows.
     */
    @SerializedName("pagination")
    private Pagination pagination;

    public List<FollowRelations> getData() {
        return data;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public Integer getTotal() {
        return total;
    }
}
