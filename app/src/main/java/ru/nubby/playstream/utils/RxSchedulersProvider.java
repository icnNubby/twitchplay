package ru.nubby.playstream.utils;

import io.reactivex.Scheduler;

public class RxSchedulersProvider {
    private final Scheduler mIOScheduler;
    private final Scheduler mUIScheduler;
    private final Scheduler mComputationScheduler;

    public RxSchedulersProvider(Scheduler ioScheduler,
                                Scheduler uiScheduler,
                                Scheduler computationScheduler) {
        mIOScheduler = ioScheduler;
        mUIScheduler = uiScheduler;
        mComputationScheduler = computationScheduler;
    }

    public Scheduler getComputationScheduler() {
        return mComputationScheduler;
    }

    public Scheduler getIOScheduler() {
        return mIOScheduler;
    }

    public Scheduler getUIScheduler() {
        return mUIScheduler;
    }
}
