package ru.nubby.playstream.presentation.login;

import dagger.Binds;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import ru.nubby.playstream.di.ActivityScoped;
import ru.nubby.playstream.di.FragmentScoped;

@Module
public abstract class LoginModule {

    @ActivityScoped
    @Binds
    abstract LoginContract.Presenter loginPresenter(LoginPresenter presenter);

}

