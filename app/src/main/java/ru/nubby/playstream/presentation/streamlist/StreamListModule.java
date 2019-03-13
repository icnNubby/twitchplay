package ru.nubby.playstream.presentation.streamlist;

import androidx.annotation.Nullable;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.android.ContributesAndroidInjector;
import io.reactivex.Observable;
import ru.nubby.playstream.di.ActivityScoped;
import ru.nubby.playstream.di.FragmentScoped;
import ru.nubby.playstream.domain.Repository;
import ru.nubby.playstream.model.StreamListNavigationState;
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


}
