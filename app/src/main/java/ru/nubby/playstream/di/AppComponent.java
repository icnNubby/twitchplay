package ru.nubby.playstream.di;

import android.app.Application;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;
import ru.nubby.playstream.PlayStreamApp;
import ru.nubby.playstream.domain.twitchapi.TwitchApiModule;

@Singleton
@Component(modules = {AppModule.class,
//        PreferencesModule.class,
        TwitchApiModule.class,
        AndroidSupportInjectionModule.class})
public interface AppComponent  extends AndroidInjector<PlayStreamApp> {

    @Component.Builder
    interface Builder {

        @BindsInstance
        AppComponent.Builder application(Application application);

        AppComponent build();
    }
}
