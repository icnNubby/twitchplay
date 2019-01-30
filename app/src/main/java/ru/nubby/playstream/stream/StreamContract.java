package ru.nubby.playstream.stream;

import java.util.HashMap;

import ru.nubby.playstream.BasePresenter;
import ru.nubby.playstream.BaseView;
import ru.nubby.playstream.model.Stream;

public interface StreamContract {
    interface View extends BaseView<Presenter> {
        void displayStream(String url);
    }

    interface Presenter extends BasePresenter {
        void pauseStream();
        void playStream(Stream stream);
        void volumeTune();

    }
}
