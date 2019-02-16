package ru.nubby.playstream.ui.streamlist;

import ru.nubby.playstream.model.UserData;
import ru.nubby.playstream.ui.BasePresenter;
import ru.nubby.playstream.ui.BaseView;

public interface StreamListActivityContract {
    interface View extends BaseView<Presenter> {
        void displayLoggedStatus(UserData user, boolean logged);
    }

    interface Presenter extends BasePresenter {
        void login(String token);
    }
}
