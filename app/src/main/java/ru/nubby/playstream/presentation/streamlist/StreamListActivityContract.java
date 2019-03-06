package ru.nubby.playstream.presentation.streamlist;

import ru.nubby.playstream.model.UserData;
import ru.nubby.playstream.presentation.BasePresenter;
import ru.nubby.playstream.presentation.BaseView;

public interface StreamListActivityContract {
    interface View extends BaseView {
        void setDefaultNavBarState(StreamListNavigationState state, boolean forceReload);
        void displayLoggedStatus(UserData user);
    }

    interface Presenter extends BasePresenter<View> {
    }
}
