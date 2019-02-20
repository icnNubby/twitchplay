package ru.nubby.playstream.ui.login;

import ru.nubby.playstream.ui.BasePresenter;
import ru.nubby.playstream.ui.BaseView;

public interface LoginContract {
    interface View extends BaseView<Presenter> {
        void loadUrl(String url);
        void handleUserInfoFetched(boolean success);
    }

    interface Presenter extends BasePresenter {
         void receivedError(String badUrl);
         boolean interceptedAnswer(String url);
    }
}
