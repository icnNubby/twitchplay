package ru.nubby.playstream.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Token info, we need it to get the M3U8 playlist
 */
public class Token {

    @SerializedName("token")
    @Expose
    private String token;

    @SerializedName("sig")
    @Expose
    private String sig;

    @SerializedName("mobile_restricted")
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
