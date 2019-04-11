package ru.nubby.playstream.presentation.user.panels;

import java.util.List;

import androidx.lifecycle.Lifecycle;
import io.reactivex.disposables.Disposable;
import ru.nubby.playstream.domain.entities.ChannelPanel;
import ru.nubby.playstream.domain.entities.UserData;
import ru.nubby.playstream.domain.interactors.PanelsInteractor;
import ru.nubby.playstream.presentation.base.BaseRxPresenter;
import ru.nubby.playstream.utils.RxSchedulersProvider;

import static ru.nubby.playstream.presentation.user.panels.PanelsContract.View.ErrorMessage.ERROR_BAD_CONNECTION;

public class PanelsPresenter extends BaseRxPresenter<PanelsContract.View> implements PanelsContract.Presenter {

    private final PanelsInteractor mPanelsInteractor;
    private final RxSchedulersProvider mRxSchedulersProvider;
    private UserData mUserData;
    private List<ChannelPanel> mFetchedPanels;

    public PanelsPresenter(PanelsInteractor panelsInteractor,
                           RxSchedulersProvider rxSchedulersProvider) {
        mPanelsInteractor = panelsInteractor;
        mRxSchedulersProvider = rxSchedulersProvider;
    }

    @Override
    public void subscribe(PanelsContract.View view, Lifecycle lifecycle, UserData user) {
        super.subscribe(view, lifecycle);
        mUserData = user;
        if (mFetchedPanels == null || mFetchedPanels.isEmpty()) {
            Disposable getPanelsTask = mPanelsInteractor
                    .getPanelsForUser(mUserData)
                    .doOnSubscribe((disposable) -> mView.displayStub())
                    .observeOn(mRxSchedulersProvider.getUiScheduler())
                    .subscribe(
                            panels -> {
                                mFetchedPanels = panels;
                                mView.displayPanels(mFetchedPanels);
                            },
                            error -> {
                                mView.displayInfoMessage(ERROR_BAD_CONNECTION);
                            });
            mCompositeDisposable.add(getPanelsTask);
        } else {
            mView.displayPanels(mFetchedPanels);
        }
    }

    @Override
    public void unsubscribe() {
        //todo?
    }

}
