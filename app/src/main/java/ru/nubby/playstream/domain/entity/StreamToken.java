package ru.nubby.playstream.domain.entity;

import com.google.gson.annotations.SerializedName;

/**
 * StreamToken info, we need it to get the M3U8 playlist
 */
public final class StreamToken {

    @SerializedName("token")
    private String token;

    @SerializedName("sig")
    private String sig;

    @SerializedName("mobile_restricted")
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
