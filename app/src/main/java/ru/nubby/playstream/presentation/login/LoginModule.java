package ru.nubby.playstream.presentation.login;

import dagger.Binds;
import dagger.Module;
import ru.nubby.playstream.di.scopes.ActivityScope;

@Module
public abstract class LoginModule {

    @ActivityScope
    @Binds
    abstract LoginContract.Presenter loginPresenter(LoginPresenter presenter);

}

