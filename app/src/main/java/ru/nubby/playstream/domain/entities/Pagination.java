package ru.nubby.playstream.domain.entities;

import com.google.gson.annotations.SerializedName;

public final class Pagination{

    @SerializedName("cursor")
    private String cursor;

    public String getCursor() {
        return cursor;
    }
}
