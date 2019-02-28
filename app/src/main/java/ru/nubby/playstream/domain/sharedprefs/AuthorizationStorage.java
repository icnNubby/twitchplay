package ru.nubby.playstream.domain.sharedprefs;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import ru.nubby.playstream.model.UserData;

public class AuthorizationStorage {
    private static final String PREFERENCES_FILENAME = "playstream";//todo change

    private static final String PREF_USER_ACCESS_TOKEN = "oauth_token";
    private static final String PREF_USER_DATA = "user_data";

    private SharedPreferences mSharedPreferences;

    public AuthorizationStorage(Context context) {
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
