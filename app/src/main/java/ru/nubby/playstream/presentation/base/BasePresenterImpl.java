package ru.nubby.playstream.presentation.base;

import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.LifecycleObserver;
import io.reactivex.disposables.CompositeDisposable;

public abstract class BasePresenterImpl<View> extends ViewModel implements LifecycleObserver,
        BasePresenter<View> {
    private View mView = null;
    private Lifecycle mLifecycle = null;
    protected CompositeDisposable mCompositeDisposable;

    @Override
    public void subscribe(View view, Lifecycle lifecycle) {
        this.mView = view;
        mCompositeDisposable = new CompositeDisposable();
        mLifecycle = lifecycle;
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

    @Nullable
    @Override
    public View getView() {
        return mView;
    }
}
