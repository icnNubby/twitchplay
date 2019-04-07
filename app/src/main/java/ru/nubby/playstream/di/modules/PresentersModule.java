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
import ru.nubby.playstream.domain.FollowsRepository;
import ru.nubby.playstream.domain.StreamsRepository;
import ru.nubby.playstream.domain.UsersRepository;
import ru.nubby.playstream.domain.interactors.AuthInteractor;
import ru.nubby.playstream.domain.interactors.NavigationStateInteractor;
import ru.nubby.playstream.domain.interactors.PreferencesInteractor;
import ru.nubby.playstream.domain.interactors.StreamsInteractor;
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
    static ViewModel streamListPresenter(StreamsRepository streamsRepository,
                                         NavigationStateInteractor navigationStateInteractor,
                                         PreferencesInteractor preferencesInteractor,
                                         RxSchedulersProvider rxSchedulersProvider) {
        return new StreamListPresenter(
                streamsRepository,
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
    static ViewModel streamPresenter(StreamsRepository streamsRepository,
                                     UsersRepository usersRepository,
                                     FollowsRepository followsRepository,
                                     StreamsInteractor streamsInteractor,
                                     PreferencesInteractor preferencesInteractor,
                                     RxSchedulersProvider rxSchedulersProvider) {
        return new StreamPresenter(
                streamsRepository,
                followsRepository,
                usersRepository,
                streamsInteractor,
                preferencesInteractor,
                rxSchedulersProvider);
    }

    @Provides
    @IntoMap
    @PresenterKey(value = ChatPresenter.class)
    static ViewModel chatPresenter(UsersRepository repository,
                                   RxSchedulersProvider rxSchedulersProvider) {
        return new ChatPresenter(
                repository,
                rxSchedulersProvider);
    }

    @Provides
    @IntoMap
    @PresenterKey(value = PreferencesPresenter.class)
    static ViewModel preferencesPresenter(StreamsRepository streamsRepository) {
        return new PreferencesPresenter(streamsRepository);
    }

    @Provides
    @IntoMap
    @PresenterKey(value = LoginPresenter.class)
    static ViewModel loginPresenter(AuthInteractor authInteractor,
                                    RxSchedulersProvider rxSchedulersProvider) {
        return new LoginPresenter(authInteractor, rxSchedulersProvider);
    }
}
