package ru.nubby.playstream.di.components;

import android.app.Application;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;
import ru.nubby.playstream.PlayStreamApp;
import ru.nubby.playstream.di.modules.ActivityBindingModule;
import ru.nubby.playstream.di.modules.AppModule;
import ru.nubby.playstream.data.DataModule;
import ru.nubby.playstream.data.sources.twitchapi.TwitchApiModule;
import ru.nubby.playstream.di.modules.RxSchedulersModule;
import ru.nubby.playstream.di.modules.ServiceBindingModule;
import ru.nubby.playstream.di.modules.PresentersModule;

@Singleton
@Component(modules = {
        AppModule.class,
        DataModule.class,
        TwitchApiModule.class,
        PresentersModule.class,
        ActivityBindingModule.class,
        ServiceBindingModule.class,
        AndroidSupportInjectionModule.class,
        RxSchedulersModule.class})

public interface AppComponent extends AndroidInjector<PlayStreamApp> {

    @Component.Builder
    interface Builder {

        @BindsInstance
        AppComponent.Builder application(Application application);

        AppComponent build();
    }
}
