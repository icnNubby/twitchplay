package ru.nubby.playstream.di.modules;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

@Module
public class RxSchedulersModule {

    @Provides
    @Named(value = "Io")
    public Scheduler provideIoScheduler(){
        return Schedulers.io();
    }

    @Provides
    @Named(value = "Computation")
    public Scheduler provideComputationScheduler(){
        return Schedulers.computation();
    }

    @Provides
    @Named(value = "Main")
    public Scheduler provideAndroidScheduler(){
        return AndroidSchedulers.mainThread();
    }

    @Provides
    @Named(value = "Test")
    public Scheduler provideTestScheduler(){
        return Schedulers.trampoline();
    }

}
