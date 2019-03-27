package ru.nubby.playstream.presentation.preferences;

import javax.inject.Inject;

import ru.nubby.playstream.data.Repository;

public class PreferencesPresenter implements PreferencesContract.Presenter {

    private PreferencesContract.View mPreferencesView;

    private Repository mRepository;

    @Inject
    PreferencesPresenter(Repository repository) {
        this.mRepository = repository;
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
