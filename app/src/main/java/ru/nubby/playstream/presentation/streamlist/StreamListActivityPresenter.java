package ru.nubby.playstream.presentation.streamlist;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import io.reactivex.disposables.Disposable;
import ru.nubby.playstream.data.Repository;
import ru.nubby.playstream.domain.entity.StreamListNavigationState;
import ru.nubby.playstream.domain.entity.UserData;
import ru.nubby.playstream.presentation.base.BaseRxPresenter;

public class StreamListActivityPresenter extends BaseRxPresenter<StreamListActivityContract.View>
        implements StreamListActivityContract.Presenter {
    private final String TAG = StreamListActivityPresenter.class.getSimpleName();

    private StreamListActivityContract.View mMainStreamListView;
    private Disposable mDisposableUserFetchTask;
    private Repository mRepository;


    public StreamListActivityPresenter(@NonNull Repository repository) {
        mRepository = repository;
    }

    @Override
    public void subscribe(StreamListActivityContract.View view, Lifecycle lifecycle) {
        super.subscribe(view, lifecycle);
        mMainStreamListView = view;
        mMainStreamListView.setNavBarState(mRepository.getCurrentNavigationState());
        mDisposableUserFetchTask = mRepository
                .getCurrentLoginInfo()
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
            mRepository.setCurrentNavigationState(state);
        }
    }
}
