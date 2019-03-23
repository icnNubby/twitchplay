package ru.nubby.playstream.presentation.streamlist;

import dagger.Binds;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import ru.nubby.playstream.di.scopes.ActivityScope;
import ru.nubby.playstream.di.scopes.FragmentScope;
import ru.nubby.playstream.presentation.streamlist.streamlistfragment.StreamListContract;
import ru.nubby.playstream.presentation.streamlist.streamlistfragment.StreamListFragment;
import ru.nubby.playstream.presentation.streamlist.streamlistfragment.StreamListPresenter;

@Module
public abstract class StreamListModule {

    @ActivityScope
    @Binds
    abstract StreamListActivityContract.Presenter streamListActivityPresenter(
            StreamListActivityPresenter streamListActivityPresenter);

    @Binds
    @ActivityScope
    public abstract StreamListContract.Presenter providePresenter(StreamListPresenter presenter);

    @FragmentScope
    @ContributesAndroidInjector
    abstract StreamListFragment streamListFragment();

}
