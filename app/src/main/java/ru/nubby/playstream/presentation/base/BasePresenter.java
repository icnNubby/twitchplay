package ru.nubby.playstream.presentation.base;

import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;

public interface BasePresenter<T> {

    /**
     * Binds presenter with a view when resumed. The Presenter will perform initialization here.
     *
     * @param view the view associated with this presenter
     */
    void subscribe(T view, Lifecycle lifecycle);

    /**
     * Drops the reference to the view when destroyed
     */
    void unsubscribe();

    /**
     * Gets attached view.
     * @return view, attached to this presenter or null.
     */
    @Nullable
    T getView();
}
