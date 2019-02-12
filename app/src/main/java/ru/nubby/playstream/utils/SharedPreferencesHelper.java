package ru.nubby.playstream.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import ru.nubby.playstream.model.UserData;

public class SharedPreferencesHelper {
    private static final String PREFERENCES_FILENAME = "playstream";

    private static final String PREF_USER_TOKEN = "oauth_token";
    private static final String PREF_USER_DATA = "user_data";

    private static SharedPreferences mSharedPreferences;
    private static SharedPreferencesHelper mSharedPreferencesHelper ;

    private SharedPreferencesHelper(Context context) {
        mSharedPreferences = context.getSharedPreferences(PREFERENCES_FILENAME, Context.MODE_PRIVATE);
    }

    public static void init(Context context) {
        if (mSharedPreferencesHelper == null) {
            mSharedPreferencesHelper = new SharedPreferencesHelper(context);
        }
    }

    public static String getUserToken() {
        return mSharedPreferences.getString(PREF_USER_TOKEN, "");
    }

    public static boolean setUserToken(String token) {
        return mSharedPreferences
                .edit()
                .putString(PREF_USER_TOKEN, token)
                .commit();
    }

    public static UserData getUserData() {
        return new Gson().fromJson(mSharedPreferences.getString(PREF_USER_DATA, ""), UserData.class);
    }

    public static boolean setUserData(String data) {
        return mSharedPreferences
                .edit()
                .putString(PREF_USER_DATA, data)
                .commit();
    }

}
