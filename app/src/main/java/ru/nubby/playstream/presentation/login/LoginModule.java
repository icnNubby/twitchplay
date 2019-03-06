package ru.nubby.playstream.presentation.login;

import dagger.Binds;
import dagger.android.ContributesAndroidInjector;
import ru.nubby.playstream.di.ActivityScoped;

public abstract class LoginModule {

    @ActivityScoped
    @ContributesAndroidInjector
    abstract LoginActivity loginActivity();

    @ActivityScoped
    @Binds
    abstract LoginContract.Presenter loginPresenter(LoginPresenter presenter);

}

