package ru.nubby.playstream.ui.login;

import java.util.List;

import ru.nubby.playstream.model.Stream;
import ru.nubby.playstream.ui.BasePresenter;
import ru.nubby.playstream.ui.BaseView;
import ru.nubby.playstream.utils.Quality;

public interface LoginContract {
    interface View extends BaseView<Presenter> {
        void loadUrl(String url);
    }

    interface Presenter extends BasePresenter {
         void receivedError(String badUrl);
         void interceptedAnswer(String url);
    }
}
