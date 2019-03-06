package ru.nubby.playstream.presentation.stream.chat;

import ru.nubby.playstream.model.ChatMessage;
import ru.nubby.playstream.presentation.BasePresenter;
import ru.nubby.playstream.presentation.BaseView;

public interface ChatContract {
    interface View extends BaseView  {
        enum InfoMessage {
            ERROR_DISCONNECTED, ERROR_RECONNECT, ERROR_FIRST_CONNECT, INFO_CONNECTED
        }
        void addChatMessage(ChatMessage message);
        void displayInfoMessage(InfoMessage message);
        void displayLoading(boolean loadingState);
    }

    interface Presenter extends BasePresenter<View> {
    }
}
