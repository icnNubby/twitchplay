package ru.nubby.playstream.di.modules;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import ru.nubby.playstream.di.scopes.ActivityScope;
import ru.nubby.playstream.presentation.login.LoginActivity;
import ru.nubby.playstream.presentation.login.LoginModule;
import ru.nubby.playstream.presentation.preferences.PreferencesActivity;
import ru.nubby.playstream.presentation.preferences.PreferencesModule;
import ru.nubby.playstream.presentation.stream.StreamChatActivity;
import ru.nubby.playstream.presentation.stream.StreamChatModule;
import ru.nubby.playstream.presentation.streamlist.StreamListActivity;
import ru.nubby.playstream.presentation.streamlist.StreamListModule;

@Module
public abstract class ActivityBindingModule {
    @ActivityScope
    @ContributesAndroidInjector(modules = LoginModule.class)
    abstract LoginActivity loginActivity();

    @ActivityScope
    @ContributesAndroidInjector(modules = PreferencesModule.class)
    abstract PreferencesActivity preferencesActivity();

    @ActivityScope
    @ContributesAndroidInjector(modules = StreamChatModule.class)
    abstract StreamChatActivity streamChatActivity();

    @ActivityScope
    @ContributesAndroidInjector(modules = StreamListModule.class)
    abstract StreamListActivity streamListActivity();
}
