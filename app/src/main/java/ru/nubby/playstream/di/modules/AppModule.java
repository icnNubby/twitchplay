package ru.nubby.playstream.di.modules;

import android.app.Application;
import android.content.Context;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import ru.nubby.playstream.services.ServicesScheduler;

@Module
public class AppModule {

    @Provides
    public Context provideContext(Application application) {
        return application;
    }

    @Provides
    public ServicesScheduler provideServiceScheduler(Context context) {
        return new ServicesScheduler(context);
    }

}
