package ru.nubby.playstream.presentation.preferences;

import android.content.Context;

import ru.nubby.playstream.presentation.BasePresenter;
import ru.nubby.playstream.presentation.BaseView;

public interface PreferencesContract {
    interface View extends BaseView {
    }

    interface Presenter extends BasePresenter<View> {
    }
}
