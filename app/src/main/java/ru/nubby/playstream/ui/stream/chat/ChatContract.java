package ru.nubby.playstream.ui.stream.chat;

import ru.nubby.playstream.model.ChatMessage;
import ru.nubby.playstream.model.Stream;
import ru.nubby.playstream.ui.BasePresenter;
import ru.nubby.playstream.ui.BaseView;

public interface ChatContract {
    interface View extends BaseView<Presenter> {
        boolean hasPresenterAttached();
        void addChatMessage(ChatMessage message);
        void displayLoading(boolean loadingState);
    }

    interface Presenter extends BasePresenter {
        void startListeningToChat(Stream stream);
    }
}