package ru.nubby.playstream.presentation.preferences;

import ru.nubby.playstream.presentation.BasePresenter;
import ru.nubby.playstream.presentation.BaseView;

public interface PreferencesContract {
    interface View extends BaseView<PreferencesContract.Presenter> {
    }

    interface Presenter extends BasePresenter {
    }
}
