package ru.nubby.playstream.data.sharedprefs;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import javax.inject.Inject;
import javax.inject.Singleton;

import ru.nubby.playstream.model.UserData;

@Singleton
public class AuthorizationStorage {
    private static final String PREFERENCES_FILENAME = "playstream";//todo change

    private static final String PREF_USER_ACCESS_TOKEN = "oauth_token";
    private static final String PREF_USER_DATA = "user_data";

    private SharedPreferences mSharedPreferences;
    private Gson mGson;

    @Inject
    public AuthorizationStorage(Context context, Gson gson) {
        mSharedPreferences = context.getSharedPreferences(PREFERENCES_FILENAME,
                Context.MODE_PRIVATE);
        mGson = gson;
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
        return mGson.fromJson(mSharedPreferences.getString(PREF_USER_DATA, ""), UserData.class);
    }

    public boolean setUserData(UserData data) {
        return mSharedPreferences
                .edit()
                .putString(PREF_USER_DATA, mGson.toJson(data, UserData.class))
                .commit();
    }

}
