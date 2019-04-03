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
import io.reactivex.Single;
import ru.nubby.playstream.data.Repository;
import ru.nubby.playstream.model.Stream;
import ru.nubby.playstream.presentation.base.PresenterFactory;
import ru.nubby.playstream.presentation.login.LoginPresenter;
import ru.nubby.playstream.presentation.preferences.PreferencesPresenter;
import ru.nubby.playstream.presentation.stream.chat.ChatPresenter;
import ru.nubby.playstream.presentation.stream.streamplayer.StreamPresenter;
import ru.nubby.playstream.presentation.streamlist.StreamListActivity;
import ru.nubby.playstream.presentation.streamlist.StreamListActivityPresenter;
import ru.nubby.playstream.presentation.streamlist.streamlistfragment.StreamListPresenter;

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
    static ViewModel streamListPresenter(Repository repository) {
        return new StreamListPresenter(repository);
    }

    @Provides
    @IntoMap
    @PresenterKey(value = StreamListActivityPresenter.class)
    static ViewModel streamListActivityPresenter(Repository repository) {
        return new StreamListActivityPresenter(repository);
    }

    @Provides
    @IntoMap
    @PresenterKey(value = StreamPresenter.class)
    static ViewModel streamPresenter(Repository repository) {
        return new StreamPresenter( repository);
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
    static ViewModel loginPresenter(Repository repository) {
        return new LoginPresenter(repository);
    }
}
