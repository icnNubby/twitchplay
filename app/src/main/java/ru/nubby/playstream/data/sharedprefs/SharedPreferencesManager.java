package ru.nubby.playstream.data.sharedprefs;

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

    private static SharedPreferences mSharedPreferences;
    private static SharedPreferencesManager sMSharedPreferencesManager;

    private SharedPreferencesManager(Context context) {
        mSharedPreferences = context.getSharedPreferences(PREFERENCES_FILENAME, Context.MODE_PRIVATE);
    }

    public static void init(Context context) {
        if (sMSharedPreferencesManager == null) {
            sMSharedPreferencesManager = new SharedPreferencesManager(context);
        }
    }

    public static String getUserAccessToken() {
        return mSharedPreferences.getString(PREF_USER_ACCESS_TOKEN, "");
    }

    public static boolean setUserAccessToken(String token) {
        return mSharedPreferences
                .edit()
                .putString(PREF_USER_ACCESS_TOKEN, token)
                .commit();
    }

    public static String getUserRefreshToken() {
        return mSharedPreferences.getString(PREF_USER_REFRESH_TOKEN, "");
    }

    public static boolean setUserRefreshToken(String token) {
        return mSharedPreferences
                .edit()
                .putString(PREF_USER_REFRESH_TOKEN, token)
                .commit();
    }

    public static Long getTokenExpiresAt() {
        return mSharedPreferences.getLong(PREF_TOKEN_EXPIRES_AT, 0L);
    }

    public static boolean setTokenExpiresAt(Long time) {
        return mSharedPreferences
                .edit()
                .putLong(PREF_USER_REFRESH_TOKEN, time)
                .commit();
    }

    public static UserData getUserData() {
        return new Gson().fromJson(mSharedPreferences.getString(PREF_USER_DATA, ""), UserData.class);
    }

    public static boolean setUserData(UserData data) {
        return mSharedPreferences
                .edit()
                .putString(PREF_USER_DATA, new Gson().toJson(data, UserData.class))
                .commit();
    }

}
