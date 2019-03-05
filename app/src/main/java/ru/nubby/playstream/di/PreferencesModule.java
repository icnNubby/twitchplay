package ru.nubby.playstream.di;

import android.content.Context;

import javax.inject.Singleton;

import androidx.annotation.NonNull;
import dagger.Module;
import dagger.Provides;
import ru.nubby.playstream.domain.sharedprefs.AuthorizationStorage;
import ru.nubby.playstream.domain.sharedprefs.DefaultPreferences;

@Module
public abstract class PreferencesModule {

    @Provides
    @Singleton
    @NonNull
    public AuthorizationStorage provideAuthorizationStorage(Context context) {
        return new AuthorizationStorage(context);
    }

    @Provides
    @Singleton
    @NonNull
    public DefaultPreferences provideDefaultPreferences(Context context) {
        return new DefaultPreferences(context);
    }

}
