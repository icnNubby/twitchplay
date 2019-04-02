package ru.nubby.playstream.presentation;

import androidx.annotation.CallSuper;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.LifecycleObserver;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public abstract class BasePresenterImpl<View> extends ViewModel implements LifecycleObserver,
        BasePresenter<View> {
    private View mView = null;
    private Lifecycle mLifecycle = null;
    protected CompositeDisposable mCompositeDisposable;

    public void subscribe(View view, Lifecycle lifecycle) {
        subscribe(view);
        mLifecycle = lifecycle;
    }

    //todo left for backward compat, remove after tests on one case
    @Override
    public void subscribe(View view) {
        this.mView = view;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onViewDestroyed() {
        mView =  null;
        mLifecycle = null;
    }

    @Override
    @CallSuper
    protected void onCleared() {
        super.onCleared();
        if (mCompositeDisposable != null) {
            mCompositeDisposable.dispose();
        }
    }

}
