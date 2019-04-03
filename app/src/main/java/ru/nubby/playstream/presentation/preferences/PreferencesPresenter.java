package ru.nubby.playstream.presentation.preferences;

import javax.inject.Inject;

import androidx.lifecycle.Lifecycle;
import ru.nubby.playstream.data.Repository;
import ru.nubby.playstream.presentation.base.BasePresenterImpl;

public class PreferencesPresenter extends BasePresenterImpl<PreferencesContract.View>
        implements PreferencesContract.Presenter {


    private Repository mRepository;

    @Inject
    public PreferencesPresenter(Repository repository) {
        this.mRepository = repository;
    }

    @Override
    public void subscribe(PreferencesContract.View view, Lifecycle lifecycle) {
        super.subscribe(view, lifecycle);
    }

    @Override
    public void unsubscribe() {
    }
}
