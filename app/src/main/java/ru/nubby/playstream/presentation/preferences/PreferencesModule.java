package ru.nubby.playstream.presentation.preferences;

import dagger.Binds;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import ru.nubby.playstream.di.scopes.ActivityScoped;
import ru.nubby.playstream.di.scopes.FragmentScoped;

@Module
public abstract class PreferencesModule {
    @FragmentScoped
    @ContributesAndroidInjector
    abstract PlaystreamPreferencesFragment preferencesFragment();

    @ActivityScoped
    @Binds
    abstract PreferencesContract.Presenter bindPresenter(PreferencesPresenter presenter);

}
