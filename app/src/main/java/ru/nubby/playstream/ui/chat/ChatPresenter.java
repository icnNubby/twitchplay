package ru.nubby.playstream.ui.chat;


import com.google.android.exoplayer2.util.Log;

import java.io.IOException;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import ru.nubby.playstream.SensitiveStorage;
import ru.nubby.playstream.ircapi.ChatChannelApi;
import ru.nubby.playstream.model.Stream;

public class ChatPresenter implements ChatContract.Presenter {
    private static final String TAG = "ChatPresenter";

    private ChatContract.View mChatView;
    private Stream mStream;
    private Disposable mChatListener;

    public ChatPresenter(ChatContract.View chatView, Stream stream) {
        mChatView = chatView;
        mStream = stream;
        mChatView.setPresenter(this);
    }

    @Override
    public void subscribe() {
        startListeningToChat();
    }

    @Override
    public void unsubscribe() {
        if (!mChatListener.isDisposed())
            mChatListener.dispose();
    }

    public void startListeningToChat() {
        try {

            mChatListener =
                    new ChatChannelApi(SensitiveStorage.getDefaultChatBotName(),
                            SensitiveStorage.getDefaultChatBotToken(),
                            mStream.getStreamerName())
                            .connect()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    result -> mChatView.addChatMessage(result),
                                    error -> Log.e(TAG, "Error while listening to chat", error));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
