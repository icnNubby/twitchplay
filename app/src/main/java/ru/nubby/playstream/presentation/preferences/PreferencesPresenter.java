package ru.nubby.playstream.presentation.preferences;

import javax.inject.Inject;

import androidx.lifecycle.Lifecycle;
import ru.nubby.playstream.domain.StreamsRepository;
import ru.nubby.playstream.domain.interactors.PreferencesInteractor;
import ru.nubby.playstream.presentation.base.BaseRxPresenter;

public class PreferencesPresenter extends BaseRxPresenter<PreferencesContract.View>
        implements PreferencesContract.Presenter {

    PreferencesInteractor mPreferencesInteractor;

    @Inject
    public PreferencesPresenter(PreferencesInteractor preferencesInteractor) {
        this.mPreferencesInteractor = preferencesInteractor;
    }

    @Override
    public void subscribe(PreferencesContract.View view, Lifecycle lifecycle) {
        super.subscribe(view, lifecycle);
    }

    @Override
    public void unsubscribe() {
    }
}
