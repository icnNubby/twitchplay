package ru.nubby.playstream.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * <a href = "https://dev.twitch.tv/docs/api/reference/#get-users-follows">
 * Query parameters here.</a>
 */
public class UserFollowsRequest {

    @SerializedName("total")
    @Expose
    private Integer total;
    /**
     * Contains an array of {@link FollowRelations} information elements.
     */
    @SerializedName("data")
    @Expose
    private List<FollowRelations> data;

    /**
     * Contains information, required to query for more follows.
     */
    @SerializedName("pagination")
    @Expose
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
