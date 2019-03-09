package ru.nubby.playstream.presentation.preferences;

import dagger.Binds;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import ru.nubby.playstream.di.ActivityScoped;
import ru.nubby.playstream.di.FragmentScoped;

@Module
public abstract class PreferencesModule {
    @FragmentScoped
    @ContributesAndroidInjector
    abstract PlaystreamPreferencesFragment preferencesFragment();

    @ActivityScoped
    @Binds
    abstract PreferencesContract.Presenter preferencesPresenter(PreferencesPresenter presenter);
}
