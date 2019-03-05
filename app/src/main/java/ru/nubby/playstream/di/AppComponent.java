package ru.nubby.playstream.di;

import dagger.Component;
import ru.nubby.playstream.domain.ProxyRepository;
import ru.nubby.playstream.domain.sharedprefs.AuthorizationStorage;
import ru.nubby.playstream.domain.sharedprefs.DefaultPreferences;

@Component(modules = {AppModule.class, AuthorizationStorage.class, DefaultPreferences.class})
public interface AppComponent {
    void inject(ProxyRepository proxyRepository);
}
