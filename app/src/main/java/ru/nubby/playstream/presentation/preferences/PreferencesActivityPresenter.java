package ru.nubby.playstream.presentation.preferences;

public class PreferencesActivityPresenter implements PreferencesContract.Presenter {

    private PreferencesContract.View mPreferencesView;

    public PreferencesActivityPresenter(PreferencesContract.View view) {
        mPreferencesView = view;
    }

    @Override
    public void subscribe() {

    }

    @Override
    public void unsubscribe() {

    }
}
