package ru.nubby.playstream.presentation.preferences;

import dagger.Binds;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import ru.nubby.playstream.di.scopes.ActivityScope;
import ru.nubby.playstream.di.scopes.FragmentScope;

@Module
public abstract class PreferencesModule {
    @FragmentScope
    @ContributesAndroidInjector
    abstract PlaystreamPreferencesFragment preferencesFragment();

}
