package ru.nubby.playstream.presentation.streamlist;

import android.util.Log;

import javax.inject.Named;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import ru.nubby.playstream.data.Repository;
import ru.nubby.playstream.domain.entity.StreamListNavigationState;
import ru.nubby.playstream.domain.entity.UserData;
import ru.nubby.playstream.domain.interactor.AuthInteractor;
import ru.nubby.playstream.domain.interactor.NavigationStateInteractor;
import ru.nubby.playstream.presentation.base.BaseRxPresenter;
import ru.nubby.playstream.utils.RxSchedulersProvider;

public class StreamListActivityPresenter extends BaseRxPresenter<StreamListActivityContract.View>
        implements StreamListActivityContract.Presenter {
    private final String TAG = StreamListActivityPresenter.class.getSimpleName();

    private final AuthInteractor mAuthInteractor;
    private final NavigationStateInteractor mNavigationStateInteractor;
    private final Scheduler mMainThreadScheduler;

    private StreamListActivityContract.View mMainStreamListView;
    private Disposable mDisposableUserFetchTask;

    public StreamListActivityPresenter(@NonNull AuthInteractor authInteractor,
                                       @NonNull NavigationStateInteractor navigationInteractor,
                                       @NonNull RxSchedulersProvider rxSchedulersProvider) {
        mAuthInteractor = authInteractor;
        mNavigationStateInteractor = navigationInteractor;
        mMainThreadScheduler = rxSchedulersProvider.getUiScheduler();
    }

    @Override
    public void subscribe(StreamListActivityContract.View view, Lifecycle lifecycle) {
        super.subscribe(view, lifecycle);
        mMainStreamListView = view;
        mMainStreamListView.setNavBarState(mNavigationStateInteractor.getCurrentNavigationState());
        mDisposableUserFetchTask = mAuthInteractor
                .getCurrentLoginInfo()
                .observeOn(mMainThreadScheduler)
                .subscribe(
                        userData -> mMainStreamListView.displayLoggedStatus(userData),
                        error -> {
                            mMainStreamListView.displayLoggedStatus(new UserData());
                            Log.e(TAG, "Error while fetching user data", error);
                        });

    }


    @Override
    public void unsubscribe() {
        if (mDisposableUserFetchTask != null && !mDisposableUserFetchTask.isDisposed()) {
            mDisposableUserFetchTask.dispose();
        }
        mMainStreamListView = null;
    }

    @Override
    public void changedNavigationState(StreamListNavigationState state,
                                       boolean isNavigationReallyClicked) {
        if (isNavigationReallyClicked) {
            mNavigationStateInteractor.setCurrentNavigationState(state);
        }
    }
}
