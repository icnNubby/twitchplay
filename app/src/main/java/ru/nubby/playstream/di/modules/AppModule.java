package ru.nubby.playstream.di.modules;

import android.app.Application;
import android.content.Context;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {

    @Provides
    public Context provideContext(Application application) {
        return application;
    }

}
