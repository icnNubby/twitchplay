package ru.nubby.playstream.presentation.login;


import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebViewDatabase;
import android.widget.ProgressBar;
import android.widget.Toast;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import dagger.android.support.DaggerAppCompatActivity;
import ru.nubby.playstream.R;
import ru.nubby.playstream.di.ActivityScoped;

public class LoginActivity extends DaggerAppCompatActivity implements LoginContract.View {
    private final String TAG = this.getClass().getSimpleName();

    private WebView mWebView;
    private ProgressBar mWebViewProgress;
    @Inject
    LoginContract.Presenter mLoginPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mWebView = findViewById(R.id.login_web_view);
        mWebViewProgress = findViewById(R.id.login_progress_bar);
        CookieManager cm = CookieManager.getInstance();
        cm.removeAllCookie();
        WebViewDatabase db = WebViewDatabase.getInstance(this);
        db.clearFormData();

        WebSettings ws = mWebView.getSettings();
        ws.setSaveFormData(false);
        mWebView.clearCache(true);
        mWebView.getSettings().setSaveFormData(false);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mWebView.getSettings().setSupportZoom(true);

        mWebView.setWebViewClient(
                new WebViewClient() { //TODO separate this class
                    @Override
                    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                        mLoginPresenter.receivedError(failingUrl);
                        mWebViewProgress.setVisibility(View.GONE);
                    }

                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        if (mLoginPresenter.interceptedAnswer(url)) {
                            mWebViewProgress.setVisibility(View.VISIBLE);
                            mWebView.setVisibility(View.GONE);
                            CookieManager cm = CookieManager.getInstance();
                            cm.removeAllCookie();

                            view.clearCache(true);
                            view.clearHistory();
                            view.clearFormData();
                            return true;
                        } else {
                            return false;
                        }
                    }

                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);
                        mWebViewProgress.setVisibility(View.GONE);
                    }
                }
        );
        mWebViewProgress.setIndeterminate(true);
        mWebViewProgress.setVisibility(View.VISIBLE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mLoginPresenter.subscribe(this);
    }

    @Override
    protected void onStop() {
        mLoginPresenter.unsubscribe();
        super.onStop();
    }

    public void loadUrl(String url) {
        mWebView.loadUrl(url);
    }

    @Override
    public boolean hasPresenterAttached() {
        return mLoginPresenter != null;
    }

    @Override
    public void handleUserInfoFetched(boolean success) {
        if (success) {
            finish();
        }
        else {
            mWebViewProgress.setVisibility(View.GONE);
            mWebView.setVisibility(View.VISIBLE);
            Toast.makeText(this, getText(R.string.login_failed), Toast.LENGTH_LONG).show();
        }
    }

}
