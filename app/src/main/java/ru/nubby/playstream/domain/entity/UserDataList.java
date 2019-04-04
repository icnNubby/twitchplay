package ru.nubby.playstream.domain.entity;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public final class UserDataList {

    @SerializedName("data")
    private List<UserData> data;

    public List<UserData> getData() {
        return data;
    }

    public void setData(List<UserData> data) {
        this.data = data;
    }
}