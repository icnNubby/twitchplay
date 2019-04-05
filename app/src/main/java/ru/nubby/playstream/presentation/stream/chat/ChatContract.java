package ru.nubby.playstream.presentation.stream.chat;

import androidx.lifecycle.Lifecycle;
import ru.nubby.playstream.domain.entities.ChatMessage;
import ru.nubby.playstream.domain.entities.Stream;
import ru.nubby.playstream.presentation.base.BasePresenter;
import ru.nubby.playstream.presentation.base.BaseView;

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
        void subscribe(View view, Lifecycle lifecycle, Stream stream);
    }
}
