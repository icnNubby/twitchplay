package ru.nubby.playstream.domain.entity;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

public final class UserToken {

    @NonNull
    @SerializedName("access_token")
    private String accessToken;
    @NonNull
    @SerializedName("refresh_token")
    private String refreshToken;
    @SerializedName("expires_in")
    private Integer expiresIn;
    @SerializedName("scope")
    private List<String> scope = new ArrayList<>();
    @SerializedName("token_type")
    private String tokenType;

    @NonNull
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(@NonNull String accessToken) {
        this.accessToken = accessToken;
    }

    @NonNull
    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(@NonNull String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Integer getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Integer expiresIn) {
        this.expiresIn = expiresIn;
    }

    public List<String> getScope() {
        return scope;
    }

    public void setScope(List<String> scope) {
        this.scope = scope;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public boolean isEmpty() {
        return accessToken.isEmpty() && refreshToken.isEmpty();
    }

}