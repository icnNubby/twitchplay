package ru.nubby.playstream.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Token {

    @SerializedName("token")
    @Expose
    private String token;

    @SerializedName("sig")
    @Expose
    private String sig;

    @SerializedName("mobileRestricted")
    @Expose
    private boolean mobileRestricted;

    public boolean isMobileRestricted() {
        return mobileRestricted;
    }

    public void setMobileRestricted(boolean mobileRestricted) {
        this.mobileRestricted = mobileRestricted;
    }

    public String getSig() {
        return sig;
    }

    public void setSig(String sig) {
        this.sig = sig;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
