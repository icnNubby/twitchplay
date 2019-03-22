package ru.nubby.playstream.di.modules;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import ru.nubby.playstream.di.scopes.ActivityScoped;
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
    @ActivityScoped
    @ContributesAndroidInjector(modules = LoginModule.class)
    abstract LoginActivity loginActivity();

    @ActivityScoped
    @ContributesAndroidInjector(modules = PreferencesModule.class)
    abstract PreferencesActivity preferencesActivity();

    @ActivityScoped
    @ContributesAndroidInjector(modules = StreamChatModule.class)
    abstract StreamChatActivity streamChatActivity();

    @ActivityScoped
    @ContributesAndroidInjector(modules = StreamListModule.class)
    abstract StreamListActivity streamListActivity();
}
