package ru.nubby.playstream.presentation;

public interface BaseView<T> {

    void setPresenter(T fragmentPresenter);
    boolean hasPresenterAttached();

}
