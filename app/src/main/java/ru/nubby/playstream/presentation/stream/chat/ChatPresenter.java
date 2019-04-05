package ru.nubby.playstream.presentation.stream.chat;


import com.google.android.exoplayer2.util.Log;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import androidx.lifecycle.Lifecycle;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import ru.nubby.playstream.SensitiveStorage;
import ru.nubby.playstream.domain.UsersRepository;
import ru.nubby.playstream.data.sources.ircapi.ChatChannelApi;
import ru.nubby.playstream.domain.entities.Stream;
import ru.nubby.playstream.presentation.base.BaseRxPresenter;

import static ru.nubby.playstream.presentation.stream.chat.ChatContract.View.InfoMessage.ERROR_DISCONNECTED;
import static ru.nubby.playstream.presentation.stream.chat.ChatContract.View.InfoMessage.ERROR_FIRST_CONNECT;
import static ru.nubby.playstream.presentation.stream.chat.ChatContract.View.InfoMessage.ERROR_RECONNECT;
import static ru.nubby.playstream.presentation.stream.chat.ChatContract.View.InfoMessage.INFO_CONNECTED;

public class ChatPresenter extends BaseRxPresenter<ChatContract.View>
        implements ChatContract.Presenter {
    private static final String TAG = ChatPresenter.class.getSimpleName();

    private static final long RETRY_DELAY_SECONDS = 10;

    private Disposable mChatListener;
    private Disposable mChatInitializer;

    private UsersRepository mRepository;
    private ChatChannelApi mChatApi = null;

    @Inject
    public ChatPresenter(UsersRepository repository) {
        mRepository = repository;
    }

    @Override
    public void subscribe(ChatContract.View view, Lifecycle lifecycle, Stream stream) {
        super.subscribe(view, lifecycle);
        Stream streamCopy = new Stream(stream);
        Single<Stream> initialStreamRequest = mRepository
                .getUserFromStreamer(stream)
                .map(updatedLogin -> {
                    streamCopy.setStreamerLogin(updatedLogin.getLogin());
                    return streamCopy;
                });
        Disposable disposableStreamAdditionalInfo = initialStreamRequest
                .doOnSubscribe(streamReturned -> mView.displayLoading(true))
                .subscribe(
                        streamReturned -> {
                            mChatApi = new ChatChannelApi( //TODO ?DI?
                                    SensitiveStorage.getDefaultChatBotName(),
                                    SensitiveStorage.getDefaultChatBotToken(),
                                    streamReturned.getStreamerLogin());
                            mChatInitializer = mChatApi
                                    .init()
                                    .retryWhen(throwableFlowable -> throwableFlowable
                                            .delay(RETRY_DELAY_SECONDS, TimeUnit.SECONDS)
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .doOnEach(throwableNotification ->
                                                    mView.displayInfoMessage(ERROR_RECONNECT)))
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(
                                            success -> {
                                                mView.displayInfoMessage(INFO_CONNECTED);
                                                listenToChat();
                                            },
                                            error -> {
                                                mView.displayInfoMessage(ERROR_FIRST_CONNECT);
                                            });
                            mCompositeDisposable.add(mChatInitializer);
                            mView.displayLoading(false);
                        },
                        error -> {
                            Log.e(TAG, "Error while fetching additional data", error);
                            mView.displayInfoMessage(ERROR_FIRST_CONNECT);
                            mView.displayLoading(false);
                        });
        mCompositeDisposable.add(disposableStreamAdditionalInfo);
    }

    @Override
    public void unsubscribe() {
    }

    private void listenToChat() {
        mChatListener = mChatApi
                .listenToChat()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> mView.addChatMessage(result),
                        error -> {
                            Log.e(TAG, "Error while listening to chat", error);
                            mView.displayInfoMessage(ERROR_DISCONNECTED);
                            mChatListener.dispose();
                            mChatInitializer = mChatApi.init()
                                    .subscribeOn(Schedulers.io())
                                    .retryWhen(throwableFlowable -> throwableFlowable
                                            .delay(10, TimeUnit.SECONDS)
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .doOnEach(throwableNotification ->
                                                    mView.displayInfoMessage(ERROR_RECONNECT)))
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(
                                            success -> {
                                                mView.displayInfoMessage(INFO_CONNECTED);
                                                listenToChat();
                                            });
                        });
        mCompositeDisposable.add(mChatListener);
    }

}
