package ru.nubby.playstream.domain.interactors;

import javax.inject.Inject;
import javax.inject.Singleton;

import androidx.annotation.NonNull;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import ru.nubby.playstream.data.sharedprefs.DefaultPreferences;
import ru.nubby.playstream.domain.entities.StreamListNavigationState;

/**
 * Business logic for main screen navigation.
 * Some kind of event bus, but for one concrete event.
 */
@Singleton
public class NavigationStateInteractor {

    private final BehaviorSubject<StreamListNavigationState> mNavigationStateObservable;
    private StreamListNavigationState mNavigationState;

    @Inject
    public NavigationStateInteractor(@NonNull DefaultPreferences defaultPreferences) {
        mNavigationState = StreamListNavigationState
                .values()[defaultPreferences.getDefaultStreamListMode()];
        mNavigationStateObservable = BehaviorSubject.create();
        mNavigationStateObservable.onNext(mNavigationState);
    }

    /**
     * Gets navigation state of stream list screen.
     * It lives here because one presenter should emit it's changes, but other should respond
     * with view changes. We make this connection by creating Observable source.
     * @return {@link StreamListNavigationState} enum.
     */
    public Observable<StreamListNavigationState> getObservableNavigationState() {
        return mNavigationStateObservable;
    }

    /**
     * Gets navigation state of stream list screen.
     * Will return default value on first retrieve.
     * @return {@link StreamListNavigationState} enum.
     */
    public StreamListNavigationState getCurrentNavigationState() {
        return mNavigationState;
    }

    /**
     * Emit next state value to all subscribers of {@link #getObservableNavigationState()}.
     * Also changes current inner state.
     * @param state state to be emitted.
     */
    public void setCurrentNavigationState(StreamListNavigationState state) {
        mNavigationState = state;
        mNavigationStateObservable.onNext(state);
    }
}
