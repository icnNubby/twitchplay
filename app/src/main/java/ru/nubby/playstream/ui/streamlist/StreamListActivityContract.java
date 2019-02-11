package ru.nubby.playstream.ui.streamlist;

import java.util.List;

import ru.nubby.playstream.model.Stream;
import ru.nubby.playstream.ui.BasePresenter;
import ru.nubby.playstream.ui.BaseView;
import ru.nubby.playstream.utils.Quality;

public interface StreamListActivityContract {
    interface View extends BaseView<Presenter> {
        void displayLoggedStatus(String loggedName, boolean logged);
    }

    interface Presenter extends BasePresenter {
        void login(String token);
    }
}
