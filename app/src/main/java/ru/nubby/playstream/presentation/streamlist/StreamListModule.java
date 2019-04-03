package ru.nubby.playstream.presentation.streamlist;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import ru.nubby.playstream.di.scopes.FragmentScope;
import ru.nubby.playstream.presentation.streamlist.streamlistfragment.StreamListFragment;

@Module
public abstract class StreamListModule {

    @FragmentScope
    @ContributesAndroidInjector
    abstract StreamListFragment streamListFragment();

}
