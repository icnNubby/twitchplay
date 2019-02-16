package ru.nubby.playstream.ui.stream.chat;

import ru.nubby.playstream.model.ChatMessage;
import ru.nubby.playstream.model.Stream;
import ru.nubby.playstream.ui.BasePresenter;
import ru.nubby.playstream.ui.BaseView;

public interface ChatContract {
    interface View extends BaseView<Presenter> {
        enum InfoMessage {
            ERROR_DISCONNECTED, ERROR_RECONNECT, ERROR_FIRST_CONNECT, INFO_CONNECTED
        }
        void addChatMessage(ChatMessage message);
        void displayInfoMessage(InfoMessage message);
        void displayLoading(boolean loadingState);
    }

    interface Presenter extends BasePresenter {
        void startListeningToChat(Stream stream);
    }
}
