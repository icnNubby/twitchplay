package ru.nubby.playstream.presentation.login;

import dagger.Binds;
import dagger.Module;
import ru.nubby.playstream.di.scopes.ActivityScoped;

@Module
public abstract class LoginModule {

    @ActivityScoped
    @Binds
    abstract LoginContract.Presenter loginPresenter(LoginPresenter presenter);

}

