package ru.nubby.playstream.ui.streamlist;

import android.util.Log;

import androidx.annotation.NonNull;
import io.reactivex.disposables.Disposable;
import ru.nubby.playstream.data.Repository;
import ru.nubby.playstream.model.UserData;

public class StreamListActivityPresenter implements StreamListActivityContract.Presenter {
    private final String TAG = StreamListActivityPresenter.class.getSimpleName();

    private StreamListActivityContract.View mMainStreamListView;
    private Disposable mDisposableUserFetchTask;
    private Repository mRepository;

    public StreamListActivityPresenter(StreamListActivityContract.View view, @NonNull Repository repository) {
        mMainStreamListView = view;
        mMainStreamListView.setPresenter(this);
        mRepository = repository;
    }


    @Override
    public void subscribe() {
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
    }
}
