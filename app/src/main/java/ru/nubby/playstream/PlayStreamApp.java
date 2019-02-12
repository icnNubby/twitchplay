package ru.nubby.playstream;

import android.app.Application;

import ru.nubby.playstream.utils.SharedPreferencesHelper;

public class PlayStreamApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferencesHelper.init(this);
    }
}
