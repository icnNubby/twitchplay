package ru.nubby.playstream.presentation.preferences;

import javax.inject.Inject;

public class PreferencesPresenter implements PreferencesContract.Presenter {

    private PreferencesContract.View mPreferencesView;

    @Inject
    PreferencesPresenter() {
    }

    @Override
    public void subscribe(PreferencesContract.View view) {
        mPreferencesView = view;
    }

    @Override
    public void unsubscribe() {
        mPreferencesView = null;
    }
}
