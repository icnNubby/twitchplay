package ru.nubby.playstream.presentation.streamlist;

import androidx.annotation.Nullable;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.android.ContributesAndroidInjector;
import ru.nubby.playstream.di.ActivityScoped;
import ru.nubby.playstream.di.FragmentScoped;
import ru.nubby.playstream.presentation.streamlist.streamlistfragment.StreamListContract;
import ru.nubby.playstream.presentation.streamlist.streamlistfragment.StreamListFragment;
import ru.nubby.playstream.presentation.streamlist.streamlistfragment.StreamListPresenter;

@Module
public abstract class StreamListModule {

    @ActivityScoped
    @Binds
    abstract StreamListActivityContract.Presenter streamListActivityPresenter(
            StreamListActivityPresenter streamListActivityPresenter);

    @FragmentScoped
    @ContributesAndroidInjector
    abstract StreamListFragment streamListFragment();

    @ActivityScoped
    @Binds
    abstract StreamListContract.Presenter streamListPresenter(
            StreamListPresenter streamListPresenter);

    @Provides
    @Nullable
    @ActivityScoped
    static StreamListNavigationState provideNavState(StreamListActivity activity) {
        return activity.getNavigationState();
    }

    @Provides
    @ActivityScoped
    static boolean provideFirstLoad(StreamListActivity activity) {
        return activity.isFirstLoad();
    }

}
