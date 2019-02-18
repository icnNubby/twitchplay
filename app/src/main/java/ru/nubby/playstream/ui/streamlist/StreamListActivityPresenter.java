package ru.nubby.playstream.ui.streamlist;

import android.util.Log;

import com.google.gson.Gson;

import androidx.annotation.NonNull;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import ru.nubby.playstream.data.GlobalRepository;
import ru.nubby.playstream.data.Repository;
import ru.nubby.playstream.model.UserData;
import ru.nubby.playstream.data.twitchapi.RemoteStreamFullInfo;
import ru.nubby.playstream.utils.SharedPreferencesHelper;

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
    public void login(String token) {
        //TODO handle logging (write to Shared etc.
        //TODO  logic to handle current login if is some
    }

    @Override
    public void subscribe() {
        String token = SharedPreferencesHelper.getUserToken();
        if (token != null && !token.equals("")) {
            UserData data = SharedPreferencesHelper.getUserData(); // TODO INJECT
            if (data == null) {
                // TODO INJECT
                mDisposableUserFetchTask = mRepository
                        .getUserDataFromToken(token)
                        .doOnSuccess(userData -> SharedPreferencesHelper.setUserData(new Gson().toJson(userData, UserData.class)))
                        .filter(userData -> userData != null)
                        .subscribe(userData -> mMainStreamListView.displayLoggedStatus(userData),
                                error -> Log.e(TAG, "Error while fetching user data", error));

            } else {
                mMainStreamListView.displayLoggedStatus(data);
            }

        } else {
            mMainStreamListView.displayLoggedStatus(new UserData()); //Empty user
        }
    }

    @Override
    public void unsubscribe() {
        if (mDisposableUserFetchTask != null && !mDisposableUserFetchTask.isDisposed())
            mDisposableUserFetchTask.dispose();
    }
}
