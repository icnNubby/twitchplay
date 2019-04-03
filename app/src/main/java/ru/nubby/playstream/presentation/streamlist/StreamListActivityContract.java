package ru.nubby.playstream.presentation.streamlist;

import ru.nubby.playstream.model.StreamListNavigationState;
import ru.nubby.playstream.model.UserData;
import ru.nubby.playstream.presentation.base.BasePresenter;
import ru.nubby.playstream.presentation.base.BaseView;

public interface StreamListActivityContract {
    interface View extends BaseView {
        void setNavBarState(StreamListNavigationState state);
        void displayLoggedStatus(UserData user);
    }

    interface Presenter extends BasePresenter<View> {
        void changedNavigationState(StreamListNavigationState state,
                                    boolean isNavigationReallyClicked); //TODO THINK!
    }
}