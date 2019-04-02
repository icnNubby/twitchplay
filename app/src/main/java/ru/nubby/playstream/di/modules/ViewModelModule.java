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
import ru.nubby.playstream.presentation.PresenterFactory;
import ru.nubby.playstream.presentation.streamlist.streamlistfragment.StreamListPresenter;

@Module
public abstract class ViewModelModule {

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @MapKey
    @interface ViewModelKey {
        Class<? extends ViewModel> value();
    }

    @Provides
    static PresenterFactory viewModelFactory(Map<Class<? extends ViewModel>, Provider<ViewModel>> providerMap) {
        return new PresenterFactory(providerMap);
    }

    @Provides
    @IntoMap
    @ViewModelKey(value = StreamListPresenter.class)
    static ViewModel streamListPresenter(Repository repository) {
        return new StreamListPresenter(repository);
    }

}
