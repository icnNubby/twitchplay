package ru.nubby.playstream.chat;

import ru.nubby.playstream.chat.ChatContract;

public class ChatPresenter implements ChatContract.Presenter {
    ChatContract.View mChatView;

    public ChatPresenter(ChatContract.View chatView) {
        mChatView = chatView;
        mChatView.setPresenter(this);
    }

    @Override
    public void subscribe() {

    }

    @Override
    public void unsubscribe() {

    }
}
