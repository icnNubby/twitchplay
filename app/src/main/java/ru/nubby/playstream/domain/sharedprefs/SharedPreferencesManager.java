package ru.nubby.playstream.domain.sharedprefs;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import ru.nubby.playstream.model.UserData;

public class SharedPreferencesManager {
    private static final String PREFERENCES_FILENAME = "playstream";

    private static final String PREF_USER_ACCESS_TOKEN = "oauth_token";
    private static final String PREF_USER_DATA = "user_data";

    //TODO probably exclude unused
    private static final String PREF_USER_REFRESH_TOKEN = "oauth_refresh_token";
    private static final String PREF_TOKEN_EXPIRES_AT = "oauth_expires_at";

    private SharedPreferences mSharedPreferences;

    public SharedPreferencesManager(Context context) {
        mSharedPreferences = context.getSharedPreferences(PREFERENCES_FILENAME, Context.MODE_PRIVATE);
    }

    public String getUserAccessToken() {
        return mSharedPreferences.getString(PREF_USER_ACCESS_TOKEN, "");
    }

    public boolean setUserAccessToken(String token) {
        return mSharedPreferences
                .edit()
                .putString(PREF_USER_ACCESS_TOKEN, token)
                .commit();
    }

    public String getUserRefreshToken() {
        return mSharedPreferences.getString(PREF_USER_REFRESH_TOKEN, "");
    }

    public boolean setUserRefreshToken(String token) {
        return mSharedPreferences
                .edit()
                .putString(PREF_USER_REFRESH_TOKEN, token)
                .commit();
    }

    public Long getTokenExpiresAt() {
        return mSharedPreferences.getLong(PREF_TOKEN_EXPIRES_AT, 0L);
    }

    public boolean setTokenExpiresAt(Long time) {
        return mSharedPreferences
                .edit()
                .putLong(PREF_USER_REFRESH_TOKEN, time)
                .commit();
    }

    public UserData getUserData() {
        return new Gson().fromJson(mSharedPreferences.getString(PREF_USER_DATA, ""), UserData.class);
    }

    public boolean setUserData(UserData data) {
        return mSharedPreferences
                .edit()
                .putString(PREF_USER_DATA, new Gson().toJson(data, UserData.class))
                .commit();
    }

}
