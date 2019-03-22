package ru.nubby.playstream.presentation.streamlist;

import dagger.Binds;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import ru.nubby.playstream.di.scopes.ActivityScoped;
import ru.nubby.playstream.di.scopes.FragmentScoped;
import ru.nubby.playstream.presentation.streamlist.streamlistfragment.StreamListContract;
import ru.nubby.playstream.presentation.streamlist.streamlistfragment.StreamListFragment;
import ru.nubby.playstream.presentation.streamlist.streamlistfragment.StreamListPresenter;

@Module
public abstract class StreamListModule {

    @ActivityScoped
    @Binds
    abstract StreamListActivityContract.Presenter streamListActivityPresenter(
            StreamListActivityPresenter streamListActivityPresenter);

    @Binds
    @ActivityScoped
    public abstract StreamListContract.Presenter providePresenter(StreamListPresenter presenter);

    @FragmentScoped
    @ContributesAndroidInjector
    abstract StreamListFragment streamListFragment();

}
