package ru.nubby.playstream.di.modules;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

import javax.inject.Provider;

import androidx.lifecycle.ViewModel;
import dagger.MapKey;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;
import ru.nubby.playstream.data.Repository;
import ru.nubby.playstream.domain.interactor.AuthInteractor;
import ru.nubby.playstream.domain.interactor.NavigationStateInteractor;
import ru.nubby.playstream.domain.interactor.PreferencesInteractor;
import ru.nubby.playstream.presentation.base.PresenterFactory;
import ru.nubby.playstream.presentation.login.LoginPresenter;
import ru.nubby.playstream.presentation.preferences.PreferencesPresenter;
import ru.nubby.playstream.presentation.stream.chat.ChatPresenter;
import ru.nubby.playstream.presentation.stream.streamplayer.StreamPresenter;
import ru.nubby.playstream.presentation.streamlist.StreamListActivityPresenter;
import ru.nubby.playstream.presentation.streamlist.streamlistfragment.StreamListPresenter;
import ru.nubby.playstream.utils.RxSchedulersProvider;

@Module
public abstract class PresentersModule {

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @MapKey
    @interface PresenterKey {
        Class<? extends ViewModel> value();
    }

    @Provides
    static PresenterFactory presenterFactory(Map<Class<? extends ViewModel>, Provider<ViewModel>> providerMap) {
        return new PresenterFactory(providerMap);
    }

    @Provides
    @IntoMap
    @PresenterKey(value = StreamListPresenter.class)
    static ViewModel streamListPresenter(Repository repository,
                                         NavigationStateInteractor navigationStateInteractor,
                                         PreferencesInteractor preferencesInteractor,
                                         RxSchedulersProvider rxSchedulersProvider) {
        return new StreamListPresenter(
                repository,
                navigationStateInteractor,
                preferencesInteractor,
                rxSchedulersProvider);
    }

    @Provides
    @IntoMap
    @PresenterKey(value = StreamListActivityPresenter.class)
    static ViewModel streamListActivityPresenter(AuthInteractor authInteractor,
                                                 NavigationStateInteractor navigationInteractor,
                                                 RxSchedulersProvider rxSchedulersProvider) {
        return new StreamListActivityPresenter(
                authInteractor,
                navigationInteractor,
                rxSchedulersProvider);
    }

    @Provides
    @IntoMap
    @PresenterKey(value = StreamPresenter.class)
    static ViewModel streamPresenter(Repository repository,
                                     PreferencesInteractor preferencesInteractor) {
        return new StreamPresenter(repository, preferencesInteractor);
    }

    @Provides
    @IntoMap
    @PresenterKey(value = ChatPresenter.class)
    static ViewModel chatPresenter(Repository repository) {
        return new ChatPresenter(repository);
    }

    @Provides
    @IntoMap
    @PresenterKey(value = PreferencesPresenter.class)
    static ViewModel preferencesPresenter(Repository repository) {
        return new PreferencesPresenter(repository);
    }

    @Provides
    @IntoMap
    @PresenterKey(value = LoginPresenter.class)
    static ViewModel loginPresenter(AuthInteractor authInteractor,
                                    RxSchedulersProvider rxSchedulersProvider) {
        return new LoginPresenter(authInteractor, rxSchedulersProvider);
    }
}
