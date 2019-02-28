package ru.nubby.playstream.presentation.streamlist;

import android.util.Log;

import androidx.annotation.NonNull;
import io.reactivex.disposables.Disposable;
import ru.nubby.playstream.domain.Repository;
import ru.nubby.playstream.model.UserData;

import static ru.nubby.playstream.presentation.streamlist.StreamListNavigationState.values;

public class StreamListActivityPresenter implements StreamListActivityContract.Presenter {
    private final String TAG = StreamListActivityPresenter.class.getSimpleName();

    private StreamListActivityContract.View mMainStreamListView;
    private Disposable mDisposableUserFetchTask;
    private Repository mRepository;

    private boolean mFirstLoad;

    public StreamListActivityPresenter(StreamListActivityContract.View view,
                                       @NonNull Repository repository,
                                       boolean firstLoad) {
        mMainStreamListView = view;
        mMainStreamListView.setPresenter(this);
        mRepository = repository;
        mFirstLoad = firstLoad;
    }

    @Override
    public void subscribe() {
        int defaultState = mRepository.getSharedPreferences().getDefaultStreamListMode();
        mMainStreamListView.setDefaultNavBarState(
                values()[defaultState], mFirstLoad);

        mDisposableUserFetchTask = mRepository
                .getCurrentLoginInfo()
                .subscribe(
                        userData -> mMainStreamListView.displayLoggedStatus(userData),
                        error -> {
                            mMainStreamListView.displayLoggedStatus(new UserData());
                            Log.e(TAG, "Error while fetching user data", error);
                        });
        mFirstLoad = false;

    }

    @Override
    public void unsubscribe() {
        if (mDisposableUserFetchTask != null && !mDisposableUserFetchTask.isDisposed())
            mDisposableUserFetchTask.dispose();
    }
}
