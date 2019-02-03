package ru.nubby.playstream.ui.chat;

import ru.nubby.playstream.model.ChatMessage;
import ru.nubby.playstream.ui.BasePresenter;
import ru.nubby.playstream.ui.BaseView;

public interface ChatContract {
    interface View extends BaseView<Presenter> {
        boolean hasPresenterAttached();
        void addChatMessage(ChatMessage message);
    }

    interface Presenter extends BasePresenter {
        public void startListeningToChat();
    }
}