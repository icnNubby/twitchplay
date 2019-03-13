package ru.nubby.playstream.presentation.streamlist;

import android.util.Log;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import io.reactivex.disposables.Disposable;
import ru.nubby.playstream.domain.Repository;
import ru.nubby.playstream.model.StreamListNavigationState;
import ru.nubby.playstream.model.UserData;

public class StreamListActivityPresenter implements StreamListActivityContract.Presenter {
    private final String TAG = StreamListActivityPresenter.class.getSimpleName();

    private StreamListActivityContract.View mMainStreamListView;
    private Disposable mDisposableUserFetchTask;
    private Repository mRepository;

    @Inject
    public StreamListActivityPresenter(@NonNull Repository repository) {
        mRepository = repository;
    }

    @Override
    public void subscribe(StreamListActivityContract.View view) {
        mMainStreamListView = view;
        mMainStreamListView.setNavBarState(mRepository.getCurrentState());
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
