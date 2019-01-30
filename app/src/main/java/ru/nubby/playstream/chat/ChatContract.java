package ru.nubby.playstream.chat;

import ru.nubby.playstream.BasePresenter;
import ru.nubby.playstream.BaseView;

public interface ChatContract {
        interface View extends BaseView<Presenter> {
                boolean hasPresenterAttached();
        }

        interface Presenter extends BasePresenter {
        }
}