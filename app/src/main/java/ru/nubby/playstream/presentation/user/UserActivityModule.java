package ru.nubby.playstream.presentation.user;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import ru.nubby.playstream.di.scopes.FragmentScope;
import ru.nubby.playstream.presentation.user.panels.PanelsFragment;
import ru.nubby.playstream.presentation.user.vods.VodsFragment;

@Module
public abstract class UserActivityModule {

    @FragmentScope
    @ContributesAndroidInjector
    abstract PanelsFragment panelsFragment();

    @FragmentScope
    @ContributesAndroidInjector
    abstract VodsFragment vodsFragment();
}
