package ru.nubby.playstream.presentation.login;

import android.util.Log;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import ru.nubby.playstream.SensitiveStorage;
import ru.nubby.playstream.domain.interactor.AuthInteractor;
import ru.nubby.playstream.presentation.base.BaseRxPresenter;
import ru.nubby.playstream.utils.RxSchedulersProvider;

public class LoginPresenter extends BaseRxPresenter<LoginContract.View>
        implements LoginContract.Presenter {
    private final String TAG = LoginPresenter.class.getSimpleName();

    private final String LOGIN_URL = "https://api.twitch.tv/kraken/oauth2/authorize?response_type=token&client_id="
                    + SensitiveStorage.getClientApiKey()
                    + "&redirect_uri=http%3A%2F%2Flocalhost&scope=user_read+user_follows_edit+user_subscriptions";

    private final AuthInteractor mAuthInteractor;
    private final Scheduler mMainThreadScheduler;

    @Inject
    public LoginPresenter(@NonNull AuthInteractor authInteractor,
                          @NonNull RxSchedulersProvider schedulersProvider) {
        mAuthInteractor = authInteractor;
        mMainThreadScheduler = schedulersProvider.getUiScheduler();
    }

    @Override
    public void subscribe(LoginContract.View view, Lifecycle lifecycle) {
        super.subscribe(view, lifecycle);
        mView.loadUrl(LOGIN_URL);
    }

    @Override
    public void unsubscribe() {
    }

    @Override
    public void receivedError(String badUrl) {
        Log.e(TAG, "Bad url response " + badUrl);
    }

    @Override
    public boolean interceptedAnswer(String url) {
        if (url.contains("#access_token=")) {

            String mAccessToken = getAccessTokenFromURL(url);
            Disposable disposableUserFetchTask = mAuthInteractor
                    .loginAttempt(mAccessToken)
                    .observeOn(mMainThreadScheduler)
                    .subscribe(
                            userData -> mView.handleUserInfoFetched(true),
                            error -> Log.e(TAG, "Error while fetching user data", error));

            mCompositeDisposable.add(disposableUserFetchTask);
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
