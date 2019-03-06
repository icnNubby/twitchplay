package ru.nubby.playstream.di;

import android.app.Application;
import android.content.Context;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {

    @Provides
    public Context bindContext(Application application) {
        return application;
    }
}
