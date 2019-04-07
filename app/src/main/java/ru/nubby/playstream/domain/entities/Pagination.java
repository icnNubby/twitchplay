package ru.nubby.playstream.domain.entities;

import com.google.gson.annotations.SerializedName;

public final class Pagination{

    @SerializedName("cursor")
    private String cursor;

    public Pagination() {

    }

    public Pagination(Pagination pagination) {
        this.cursor = pagination.getCursor();
    }

    public String getCursor() {
        return cursor;
    }
}
