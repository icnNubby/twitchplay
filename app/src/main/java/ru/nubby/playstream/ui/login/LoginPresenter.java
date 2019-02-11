package ru.nubby.playstream.ui.login;

import android.util.Log;

import ru.nubby.playstream.SensitiveStorage;

public class LoginPresenter implements LoginContract.Presenter {
    private final String TAG = LoginPresenter.class.getSimpleName();

    private final String LOGIN_URL = "https://api.twitch.tv/kraken/oauth2/authorize?response_type=token&client_id=" + SensitiveStorage.getClientApiKey()+ "&redirect_uri=http%3A%2F%2Flocalhost&scope=user_read+user_follows_edit+user_subscriptions";

    LoginContract.View mLoginView;

    LoginPresenter(LoginContract.View loginView) {
        mLoginView = loginView;
        mLoginView.setPresenter(this);
    }

    @Override
    public void subscribe() {
        mLoginView.loadUrl(LOGIN_URL);
    }

    @Override
    public void unsubscribe() {

    }

    @Override
    public void receivedError(String badUrl) {
        Log.e(TAG, "Bad url response " + badUrl);
    }

    @Override
    public void interceptedAnswer(String url) {
        Log.d(TAG, "GOOD url response " + url);
    }
}
