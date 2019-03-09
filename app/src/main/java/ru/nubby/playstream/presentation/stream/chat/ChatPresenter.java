package ru.nubby.playstream.presentation.stream.chat;


import com.google.android.exoplayer2.util.Log;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import ru.nubby.playstream.SensitiveStorage;
import ru.nubby.playstream.domain.Repository;
import ru.nubby.playstream.domain.ircapi.ChatChannelApi;
import ru.nubby.playstream.model.Stream;

import static ru.nubby.playstream.presentation.stream.chat.ChatContract.View.InfoMessage.ERROR_DISCONNECTED;
import static ru.nubby.playstream.presentation.stream.chat.ChatContract.View.InfoMessage.ERROR_FIRST_CONNECT;
import static ru.nubby.playstream.presentation.stream.chat.ChatContract.View.InfoMessage.ERROR_RECONNECT;
import static ru.nubby.playstream.presentation.stream.chat.ChatContract.View.InfoMessage.INFO_CONNECTED;

public class ChatPresenter implements ChatContract.Presenter {
    private static final String TAG = ChatPresenter.class.getSimpleName();

    private ChatContract.View mChatView;
    private Disposable mChatListener;
    private Disposable mDisposableStreamAdditionalInfo;
    private Single<Stream> mStreamSingle;
    private ChatChannelApi mChatApi = null;
    private Disposable mChatInitializer;

    @Inject
    public ChatPresenter(@Nullable  Single<Stream> stream) {
        mStreamSingle = stream;
    }

    @Override
    public void subscribe(ChatContract.View view) {
        mChatView = view;
        mDisposableStreamAdditionalInfo = mStreamSingle
                .doOnSubscribe(streamReturned -> mChatView.displayLoading(true))
                .subscribe(
                        streamReturned -> {
                            mChatApi = new ChatChannelApi( //TODO ?DI?
                                    SensitiveStorage.getDefaultChatBotName(),
                                    SensitiveStorage.getDefaultChatBotToken(),
                                    streamReturned.getStreamerLogin());
                            mChatInitializer = mChatApi
                                    .init()
                                    .retryWhen(throwableFlowable -> throwableFlowable
                                            .delay(10, TimeUnit.SECONDS)
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .doOnEach(throwableNotification ->
                                                    mChatView.displayInfoMessage(ERROR_RECONNECT)))
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(
                                            success -> {
                                                mChatView.displayInfoMessage(INFO_CONNECTED);
                                                listenToChat();
                                            },
                                            error -> {
                                                mChatView.displayInfoMessage(ERROR_FIRST_CONNECT);
                                            });
                            mChatView.displayLoading(false);
                        },
                        error -> {
                            Log.e(TAG, "Error while fetching additional data", error);
                            mChatView.displayInfoMessage(ERROR_FIRST_CONNECT);
                            mChatView.displayLoading(false);
                        });
    }

    @Override
    public void unsubscribe() {
        //TODO composite disposal
        if (mChatListener != null && !mChatListener.isDisposed()) {
            mChatListener.dispose();
        }
        if (mChatInitializer != null && !mChatInitializer.isDisposed()) {
            mChatInitializer.dispose();
        }
        if (mDisposableStreamAdditionalInfo != null && !mDisposableStreamAdditionalInfo.isDisposed()) {
            mDisposableStreamAdditionalInfo.dispose();
        }
        mChatView = null;
    }

    private void listenToChat() {
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
                                    .subscribeOn(Schedulers.io())
                                    .retryWhen(throwableFlowable -> throwableFlowable
                                            .delay(10, TimeUnit.SECONDS)
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .doOnEach(throwableNotification ->
                                                    mChatView.displayInfoMessage(ERROR_RECONNECT)))
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(
                                            success -> {
                                                mChatView.displayInfoMessage(INFO_CONNECTED);
                                                listenToChat();
                                            });
                        });
    }

}
