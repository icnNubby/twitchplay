package ru.nubby.playstream.ui.login;


import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebViewDatabase;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import ru.nubby.playstream.R;

public class LoginActivity extends AppCompatActivity implements LoginContract.View {
    private final String TAG = this.getClass().getSimpleName();

    private WebView mWebView;
    private ProgressBar mWebViewProgress;
    private LoginContract.Presenter mLoginPresenter;

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
                new WebViewClient() {
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

        new LoginPresenter(this); //TODO INJECT
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLoginPresenter.subscribe();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mLoginPresenter.unsubscribe();
    }

    public void loadUrl(String url) {
        mWebView.loadUrl(url);
    }

    @Override
    public void setPresenter(LoginContract.Presenter presenter) {
        mLoginPresenter = presenter;
    }

    @Override
    public boolean hasPresenterAttached() {
        return mLoginPresenter != null;
    }

    @Override
    public void userInfoFetched(boolean success) {
        if (success) {
            finish();
        }
        else {
            mWebViewProgress.setVisibility(View.GONE);
            mWebView.setVisibility(View.VISIBLE);
            //todo error messaging
        }
    }

}
