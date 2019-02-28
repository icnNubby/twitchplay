package ru.nubby.playstream.presentation.preferences;

public class PreferencesPresenter implements PreferencesContract.Presenter {

    private PreferencesContract.View mPreferencesView;

    public PreferencesPresenter(PreferencesContract.View view) {
        mPreferencesView = view;
        mPreferencesView.setPresenter(this);
    }

    @Override
    public void subscribe() {

    }

    @Override
    public void unsubscribe() {

    }
}
