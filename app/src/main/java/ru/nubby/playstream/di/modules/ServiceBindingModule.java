package ru.nubby.playstream.di.modules;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import ru.nubby.playstream.services.NotificationService;
import ru.nubby.playstream.services.SyncUserDataService;

@Module
public abstract class ServiceBindingModule {

    @ContributesAndroidInjector
    abstract SyncUserDataService provideSyncService();

    @ContributesAndroidInjector
    abstract NotificationService provideNotificationService();

}
