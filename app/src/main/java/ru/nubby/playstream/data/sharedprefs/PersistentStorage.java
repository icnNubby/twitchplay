package ru.nubby.playstream.data.sharedprefs;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import ru.nubby.playstream.model.Stream;

@Singleton
public class PersistentStorage {

    private static final String PREFERENCES_FILENAME = "pref_storage";//todo change

    private static final String PREF_STREAM_LIST = "stream_list";

    private SharedPreferences mSharedPreferences;
    private Gson mGson;

    @Inject
    PersistentStorage(Context context, Gson gson) {
        mSharedPreferences = context.getSharedPreferences(PREFERENCES_FILENAME,
                Context.MODE_PRIVATE);
        mGson = gson;
    }

    public void setStreamList(List<Stream> streamList) {
        mSharedPreferences
                .edit()
                .putString(PREF_STREAM_LIST, mGson.toJson(streamList))
                .apply();
    }

    public List<Stream> getStreamList() {
        String jsonnedStreamList = mSharedPreferences.getString(PREF_STREAM_LIST,"");
        Type type = new TypeToken<ArrayList<Stream>>(){}.getType();
        return mGson.fromJson(jsonnedStreamList, type);
    }
}
