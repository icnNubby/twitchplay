package ru.nubby.playstream.ui.stream.chat;


import com.google.android.exoplayer2.util.Log;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import ru.nubby.playstream.SensitiveStorage;
import ru.nubby.playstream.ircapi.ChatChannelApi;
import ru.nubby.playstream.model.ChatMessage;
import ru.nubby.playstream.model.Stream;

public class ChatPresenter implements ChatContract.Presenter {
    private static final String TAG = "ChatPresenter";

    private ChatContract.View mChatView;
    private Disposable mChatListener;
    private Disposable mDisposableStreamAdditionalInfo;
    private Single<Stream> mStreamSingle;

    public ChatPresenter(ChatContract.View chatView, Single<Stream> stream) {
        mChatView = chatView;
        mStreamSingle = stream;
        mChatView.setPresenter(this);
    }

    @Override
    public void subscribe() {
        mDisposableStreamAdditionalInfo = mStreamSingle
                .doOnSubscribe(streamReturned -> mChatView.displayLoading(true))
                .subscribe(streamReturned -> {
                            startListeningToChat(streamReturned);
                            mChatView.displayLoading(false);
                        },
                        error -> android.util.Log.e(TAG, "Error while fetching additional data ", error));    }

    @Override
    public void unsubscribe() {
        if (!mChatListener.isDisposed()) mChatListener.dispose();
        if (!mDisposableStreamAdditionalInfo.isDisposed())
            mDisposableStreamAdditionalInfo.dispose();

    }

    public void startListeningToChat(Stream stream) {

        mChatListener =
                new ChatChannelApi(SensitiveStorage.getDefaultChatBotName(),
                        SensitiveStorage.getDefaultChatBotToken(),
                        stream.getStreamerLogin())
                        .connect()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                result -> mChatView.addChatMessage(result),
                                error -> {
                                    Log.e(TAG, "Error while listening to chat", error);
                                    mChatView.addChatMessage(new ChatMessage("ERROR:", "DISCONNECTED FROM CHAT", ""));
                                    startListeningToChat(stream);
                                });

    }

}
