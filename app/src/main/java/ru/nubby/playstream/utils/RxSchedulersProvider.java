package ru.nubby.playstream.utils;

import javax.inject.Inject;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class RxSchedulersProvider {
    private final Scheduler mIoScheduler;
    private final Scheduler mUiScheduler;
    private final Scheduler mComputationScheduler;

    @Inject
    public RxSchedulersProvider() {
        mIoScheduler = Schedulers.io();
        mUiScheduler = AndroidSchedulers.mainThread();
        mComputationScheduler = Schedulers.computation();
    }

    public Scheduler getComputationScheduler() {
        return mComputationScheduler;
    }

    public Scheduler getIoScheduler() {
        return mIoScheduler;
    }

    public Scheduler getUiScheduler() {
        return mUiScheduler;
    }
}
