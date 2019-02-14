package ru.nubby.playstream.ui;

public interface BaseView<T> {

    void setPresenter(T fragmentPresenter);
    boolean hasPresenterAttached();

}
