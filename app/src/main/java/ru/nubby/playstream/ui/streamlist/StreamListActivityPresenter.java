package ru.nubby.playstream.ui.streamlist;

import android.util.Log;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import io.reactivex.disposables.Disposable;
import ru.nubby.playstream.model.UserData;
import ru.nubby.playstream.twitchapi.RemoteStreamFullInfo;
import ru.nubby.playstream.utils.SharedPreferencesHelper;

public class StreamListActivityPresenter implements StreamListActivityContract.Presenter {
    private final String TAG = StreamListActivityPresenter.class.getSimpleName();

    private StreamListActivityContract.View mMainStreamListView;
    private Disposable mDisposableUserFetchTask;

    public StreamListActivityPresenter(StreamListActivityContract.View view) {
        mMainStreamListView = view;
        mMainStreamListView.setPresenter(this);
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
                RemoteStreamFullInfo info = new RemoteStreamFullInfo();
                mDisposableUserFetchTask = info
                        .getUserDataFromToken(token)
                        .doOnSuccess(userData -> SharedPreferencesHelper.setUserData(new Gson().toJson(userData, UserData.class)))
                        .filter(userData -> userData != null)
                        .subscribe(userData -> mMainStreamListView.displayLoggedStatus(data, true),
                                error -> Log.e(TAG, "Error while fetching user data", error));

            } else {
                mMainStreamListView.displayLoggedStatus(data, true);
            }

        } else {
            mMainStreamListView.displayLoggedStatus(null,false);
        }
    }

    @Override
    public void unsubscribe() {
        if (mDisposableUserFetchTask != null && !mDisposableUserFetchTask.isDisposed())
            mDisposableUserFetchTask.dispose();
    }
}
