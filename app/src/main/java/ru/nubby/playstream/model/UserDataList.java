package ru.nubby.playstream.model;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserDataList {

    @SerializedName("data")
    @Expose
    private List<UserData> data = null;

    public List<UserData> getData() {
        return data;
    }

    public void setData(List<UserData> data) {
        this.data = data;
    }
}