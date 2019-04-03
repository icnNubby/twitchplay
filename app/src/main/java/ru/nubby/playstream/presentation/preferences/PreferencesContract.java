package ru.nubby.playstream.presentation.preferences;

import ru.nubby.playstream.presentation.base.BasePresenter;
import ru.nubby.playstream.presentation.base.BaseView;

public interface PreferencesContract {
    interface View extends BaseView {
    }

    interface Presenter extends BasePresenter<View> {
    }
}
