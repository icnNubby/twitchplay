package ru.nubby.playstream.di.modules;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import ru.nubby.playstream.services.SyncService;

@Module
public abstract class ServiceBindingModule {

    @ContributesAndroidInjector
    abstract SyncService provideMyService();
}
