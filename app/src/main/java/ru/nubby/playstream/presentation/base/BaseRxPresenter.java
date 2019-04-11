package ru.nubby.playstream.presentation.base;

import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ViewModel;
import io.reactivex.disposables.CompositeDisposable;

public abstract class BaseRxPresenter<View> extends ViewModel implements LifecycleObserver,
        BasePresenter<View> {

    private static final String TAG = BaseRxPresenter.class.getSimpleName();

    protected View mView = null;
    private Lifecycle mLifecycle = null;
    protected CompositeDisposable mCompositeDisposable;

    @Override
    public void subscribe(View view, Lifecycle lifecycle) {
        this.mView = view;
        if (mCompositeDisposable != null && !mCompositeDisposable.isDisposed()) {
            mCompositeDisposable.clear();
        }
        mCompositeDisposable = new CompositeDisposable();
        mLifecycle = lifecycle;
        mLifecycle.addObserver(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onViewStopped() {
        if (mCompositeDisposable != null) {
            mCompositeDisposable.clear();
        }
    }

    @Override
    @CallSuper
    protected void onCleared() {
        super.onCleared();
        mView =  null;
        mLifecycle = null;
    }

    @Nullable
    @Override
    public View getView() {
        return mView;
    }
}
