package ru.nubby.playstream.ui.stream.chat;


import com.google.android.exoplayer2.util.Log;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import ru.nubby.playstream.SensitiveStorage;
import ru.nubby.playstream.ircapi.ChatChannelApi;
import ru.nubby.playstream.model.Stream;

import static ru.nubby.playstream.ui.stream.chat.ChatContract.View.InfoMessage.ERROR_DISCONNECTED;
import static ru.nubby.playstream.ui.stream.chat.ChatContract.View.InfoMessage.ERROR_FIRST_CONNECT;
import static ru.nubby.playstream.ui.stream.chat.ChatContract.View.InfoMessage.ERROR_RECONNECT;
import static ru.nubby.playstream.ui.stream.chat.ChatContract.View.InfoMessage.INFO_CONNECTED;

public class ChatPresenter implements ChatContract.Presenter {
    private static final String TAG = ChatPresenter.class.getSimpleName();

    private ChatContract.View mChatView;
    private Disposable mChatListener;
    private Disposable mDisposableStreamAdditionalInfo;
    private Single<Stream> mStreamSingle;
    private ChatChannelApi mChatApi = null;
    private Disposable mChatInitializer;

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
                            mChatApi = new ChatChannelApi(SensitiveStorage.getDefaultChatBotName(),
                                    SensitiveStorage.getDefaultChatBotToken(),
                                    streamReturned.getStreamerLogin());
                            mChatInitializer = mChatApi
                                    .init()
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(success -> {
                                                mChatView.displayInfoMessage(INFO_CONNECTED);
                                                startListeningToChat(streamReturned);
                                            },
                                            error -> {
                                                mChatView.displayInfoMessage(ERROR_FIRST_CONNECT);
                                                Log.e(TAG, "Error while connecting", error);
                                            });
                            mChatView.displayLoading(false);
                        },
                        error -> Log.e(TAG, "Error while fetching additional data", error));
    }

    @Override
    public void unsubscribe() {
        if (mChatListener != null && !mChatListener.isDisposed()) mChatListener.dispose();
        if (mChatInitializer != null && !mChatInitializer.isDisposed()) mChatInitializer.dispose();
        if (mDisposableStreamAdditionalInfo != null && !mDisposableStreamAdditionalInfo.isDisposed()) {
            mDisposableStreamAdditionalInfo.dispose();
        }
        try {
            if (mChatApi != null)
                mChatApi.closeConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void startListeningToChat(Stream stream) {

        mChatListener = mChatApi
                .listenToChat()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> mChatView.addChatMessage(result),
                        error -> {
                            Log.e(TAG, "Error while listening to chat", error);
                            mChatView.displayInfoMessage(ERROR_DISCONNECTED);
                            mChatListener.dispose();
                            mChatInitializer = mChatApi.init()
                                    .delay(10, TimeUnit.SECONDS)
                                    .retry()
                                    .subscribe(success -> startListeningToChat(stream),
                                            errorReconnect -> {
                                                mChatView.displayInfoMessage(ERROR_RECONNECT);
                                                Log.e(TAG, "Error while reconnecting", errorReconnect);
                                            });
                        });

    }

}
