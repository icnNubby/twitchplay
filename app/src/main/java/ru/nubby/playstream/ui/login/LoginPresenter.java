package ru.nubby.playstream.ui.login;

import android.util.Log;

import com.google.gson.Gson;

import io.reactivex.disposables.Disposable;
import ru.nubby.playstream.SensitiveStorage;
import ru.nubby.playstream.data.Repository;
import ru.nubby.playstream.model.UserData;
import ru.nubby.playstream.data.twitchapi.RemoteStreamFullInfo;
import ru.nubby.playstream.utils.SharedPreferencesHelper;

public class LoginPresenter implements LoginContract.Presenter {
    private final String TAG = LoginPresenter.class.getSimpleName();

    private final String LOGIN_URL = "https://api.twitch.tv/kraken/oauth2/authorize?response_type=token&client_id=" + SensitiveStorage.getClientApiKey() + "&redirect_uri=http%3A%2F%2Flocalhost&scope=user_read+user_follows_edit+user_subscriptions";

    private LoginContract.View mLoginView;
    private Disposable mDisposableUserFetchTask;

    private Repository mRemoteStreamFullInfo;

    LoginPresenter(LoginContract.View loginView, Repository repository) {
        mLoginView = loginView;
        mLoginView.setPresenter(this);
        mRemoteStreamFullInfo = repository;
    }

    @Override
    public void subscribe() {
        mLoginView.loadUrl(LOGIN_URL);
    }

    @Override
    public void unsubscribe() {
        if (mDisposableUserFetchTask != null && mDisposableUserFetchTask.isDisposed()) {
            mDisposableUserFetchTask.dispose();
        }
    }

    @Override
    public void receivedError(String badUrl) {
        Log.e(TAG, "Bad url response " + badUrl);
    }

    @Override
    public boolean interceptedAnswer(String url) {
        if (url.contains("#access_token=")) {
            String mAccessToken = getAccessTokenFromURL(url);
            SharedPreferencesHelper.setUserToken(mAccessToken);
            Log.d(TAG, "Token response " + mAccessToken);
            //TODO make interactor
            mDisposableUserFetchTask = mRemoteStreamFullInfo
                    .getUserDataFromToken(mAccessToken)
                    .doOnSuccess(userData -> SharedPreferencesHelper.setUserData(new Gson().toJson(userData, UserData.class)))
                    .subscribe(userData -> mLoginView.userInfoFetched(true),
                            error -> Log.e(TAG, "Error while fetching user data", error));
            return true;
        }
        return false;
    }

    private String getAccessTokenFromURL(String url) {
        String startIdentifier = "access_token";
        String endIdentifier = "&scope";

        int startIndex = url.indexOf(startIdentifier) + startIdentifier.length() + 1;
        int lastIndex = url.indexOf(endIdentifier);

        return url.substring(startIndex, lastIndex);
    }
}
