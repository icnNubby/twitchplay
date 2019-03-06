package ru.nubby.playstream.presentation.login;

import ru.nubby.playstream.presentation.BasePresenter;
import ru.nubby.playstream.presentation.BaseView;

public interface LoginContract {
    interface View extends BaseView {
        void loadUrl(String url);
        void handleUserInfoFetched(boolean success);
    }

    interface Presenter extends BasePresenter<View> {
         void receivedError(String badUrl);
         boolean interceptedAnswer(String url);
    }
}
