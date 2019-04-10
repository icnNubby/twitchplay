package ru.nubby.playstream.presentation.streamlist.streamlistfragment;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import ru.nubby.playstream.domain.entities.Pagination;
import ru.nubby.playstream.domain.entities.Stream;
import ru.nubby.playstream.domain.entities.StreamListNavigationState;
import ru.nubby.playstream.domain.interactors.NavigationStateInteractor;
import ru.nubby.playstream.domain.interactors.PreferencesInteractor;
import ru.nubby.playstream.domain.interactors.StreamsInteractor;
import ru.nubby.playstream.presentation.base.BaseRxPresenter;
import ru.nubby.playstream.utils.RxSchedulersProvider;

import static ru.nubby.playstream.presentation.streamlist.streamlistfragment.StreamListContract.View.ErrorMessage.ERROR_BAD_CONNECTION;


public class StreamListPresenter extends BaseRxPresenter<StreamListContract.View>
        implements StreamListContract.Presenter {

    private static final String TAG = StreamListPresenter.class.getSimpleName();
    private final long UPDATE_INTERVAL_MILLIS = 1000 * 60 * 5; // 5 minutes

    private final StreamsInteractor mStreamsInteractor;
    private final NavigationStateInteractor mNavigationStateInteractor;
    private final PreferencesInteractor mPreferencesInteractor;
    private final RxSchedulersProvider mRxSchedulerProvider;

    private Disposable mDisposableFetchingTask;

    private Pagination mPagination;

    private List<Stream> mCurrentStreamList;
    private Map<String, Stream> mCurrentStreamMap;
    private Observable<StreamListNavigationState> mListStateObservable;
    private boolean mForceReload = false;

    public StreamListPresenter(@NonNull StreamsInteractor streamsInteractor,
                               @NonNull NavigationStateInteractor navigationStateInteractor,
                               @NonNull PreferencesInteractor preferencesInteractor,
                               @NonNull RxSchedulersProvider rxSchedulersProvider) {
        mStreamsInteractor = streamsInteractor;
        mNavigationStateInteractor = navigationStateInteractor;
        mPreferencesInteractor = preferencesInteractor;
        mRxSchedulerProvider = rxSchedulersProvider;
        mListStateObservable = mNavigationStateInteractor.getObservableNavigationState();
    }

    @Override
    public void subscribe(StreamListContract.View view, Lifecycle lifecycle, long interval) {
        super.subscribe(view, lifecycle);
        mView.setPreviewSize(mPreferencesInteractor.getPreviewSize());

        mForceReload = (interval >= UPDATE_INTERVAL_MILLIS ||
                interval == 0 ||
                mCurrentStreamList == null ||
                mCurrentStreamList.isEmpty());

        Disposable disposableListState = mListStateObservable
                .observeOn(mRxSchedulerProvider.getUiScheduler())
                .subscribe(streamListNavigationState -> {
                    if (mForceReload) {
                        updateStreams();
                    } else {
                        mForceReload = true;
                        mView.displayStreamList(mCurrentStreamList);
                    }
                });

        mCompositeDisposable.add(disposableListState);
    }

    @Override
    public void unsubscribe() {
        Log.d(TAG, "unsubscribe: ");
    }

    @Override
    public void updateStreams() {
        if (mDisposableFetchingTask != null) {
            mCompositeDisposable.remove(mDisposableFetchingTask);
            mDisposableFetchingTask.dispose();
        }
        StreamListNavigationState state = mNavigationStateInteractor.getCurrentNavigationState();
        if (state == StreamListNavigationState.MODE_TOP) {
            getTopStreams();
        } else if (state == StreamListNavigationState.MODE_FAVOURITES) {
            getFollowedStreams();
        }
    }

    @Override
    public void getFollowedStreams() {
        if (mDisposableFetchingTask != null) {
            mCompositeDisposable.remove(mDisposableFetchingTask);
            mDisposableFetchingTask.dispose();
        }

        mDisposableFetchingTask = mStreamsInteractor
                .getLiveStreamsFollowedByUser()
                .observeOn(mRxSchedulerProvider.getUiScheduler())
                .doOnSubscribe(disposable -> {
                    if (!disposable.isDisposed()) {
                        mView.clearStreamList();
                        mCurrentStreamList = new ArrayList<>();
                        mCurrentStreamMap = new HashMap<>();
                        mView.setupProgressBar(true);
                    }
                })
                .doFinally(() -> mView.setupProgressBar(false))
                .subscribe(
                        streams -> {
                            checkAndAddStreams(streams);
                            mView.displayStreamList(mCurrentStreamList);

                            mPagination = null;
                        },
                        error -> {
                            mView.displayError(ERROR_BAD_CONNECTION);
                            Log.e(TAG, "Error while fetching user follows ", error);
                        });
        mCompositeDisposable.add(mDisposableFetchingTask);
    }

    @Override
    public void getTopStreams() {
        mDisposableFetchingTask = mStreamsInteractor
                .getTopStreams()
                .observeOn(mRxSchedulerProvider.getUiScheduler())
                .doOnSubscribe(disposable -> {
                    if (!disposable.isDisposed()) {
                        mView.clearStreamList();
                        mCurrentStreamList = new ArrayList<>();
                        mCurrentStreamMap = new HashMap<>();
                        mView.setupProgressBar(true);
                    }
                })
                .doFinally(() -> mView.setupProgressBar(false))
                .subscribe(
                        streams -> {
                            checkAndAddStreams(streams.getData());
                            mView.displayStreamList(mCurrentStreamList);
                            mPagination = streams.getPagination();
                        },
                        error -> {
                            mView.displayError(ERROR_BAD_CONNECTION);
                            Log.e(TAG, "Error while fetching streams", error);
                        });
        mCompositeDisposable.add(mDisposableFetchingTask);
    }

    @Override
    public void getMoreStreams() {
        StreamListNavigationState state = mNavigationStateInteractor.getCurrentNavigationState();
        if (state == StreamListNavigationState.MODE_TOP && mPagination != null) {
            if (mDisposableFetchingTask != null) {
                mCompositeDisposable.remove(mDisposableFetchingTask);
                mDisposableFetchingTask.dispose();
            }
            mDisposableFetchingTask = mStreamsInteractor
                    .getTopStreams(mPagination)
                    .observeOn(mRxSchedulerProvider.getUiScheduler())
                    .subscribe(
                            streams -> {
                                List<Stream> moreStreams = checkAndAddStreams(streams.getData());
                                mView.addStreamList(moreStreams);
                                mPagination = streams.getPagination();
                            },
                            error -> {
                                mView.displayError(ERROR_BAD_CONNECTION);
                                Log.e(TAG, "Error while fetching more streams", error);
                            });
            mCompositeDisposable.add(mDisposableFetchingTask);
        } else {
            Log.e(TAG, "Wrong mode to fetch more streams, pagination cursor = " +
                    mPagination + ", state = " + state);
        }
    }

    /**
     * Checks incoming list for duplicates and add to current, also removing duplicates.
     *
     * @param streams {@link Stream} List.
     * @return {@link Stream} list of added streams, without duplicates
     */
    private List<Stream> checkAndAddStreams(List<Stream> streams) {
        List<Stream> moreStreams = new ArrayList<>(streams);
        Iterator<Stream> checker = moreStreams.iterator();
        while (checker.hasNext()) {
            Stream nextStream = checker.next();
            if (mCurrentStreamMap.containsKey(nextStream.getUserId())) {
                checker.remove();
            } else {
                mCurrentStreamMap.put(nextStream.getUserId(),
                        nextStream);
            }
        }
        mCurrentStreamList.addAll(moreStreams);
        return moreStreams;
    }
}
