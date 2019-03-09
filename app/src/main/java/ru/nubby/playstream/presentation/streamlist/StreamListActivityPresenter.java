package ru.nubby.playstream.presentation.streamlist;

import android.util.Log;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import dagger.Lazy;
import io.reactivex.disposables.Disposable;
import ru.nubby.playstream.domain.Repository;
import ru.nubby.playstream.model.UserData;
import ru.nubby.playstream.presentation.streamlist.streamlistfragment.StreamListContract;

import static ru.nubby.playstream.presentation.streamlist.StreamListNavigationState.values;

public class StreamListActivityPresenter implements StreamListActivityContract.Presenter {
    private final String TAG = StreamListActivityPresenter.class.getSimpleName();

    private StreamListActivityContract.View mMainStreamListView;
    private Disposable mDisposableUserFetchTask;
    private Repository mRepository;

    private Lazy<Boolean> mFirstLoad;

    @Inject
    public StreamListActivityPresenter(@NonNull Repository repository,
                                       Lazy<Boolean> firstLoad) {
        mRepository = repository;
        mFirstLoad = firstLoad;
    }

    @Override
    public void subscribe(StreamListActivityContract.View view) {
        mMainStreamListView = view;
        int defaultState = mRepository.getSharedPreferences().getDefaultStreamListMode();
        mMainStreamListView.setDefaultNavBarState(
                values()[defaultState]);

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
        if (mDisposableUserFetchTask != null && !mDisposableUserFetchTask.isDisposed())
            mDisposableUserFetchTask.dispose();
        mMainStreamListView = null;
    }
}
