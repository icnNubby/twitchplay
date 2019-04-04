package ru.nubby.playstream.presentation.streamlist.streamlistfragment;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import androidx.lifecycle.Lifecycle;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import ru.nubby.playstream.data.Repository;
import ru.nubby.playstream.domain.entity.Pagination;
import ru.nubby.playstream.domain.entity.Stream;
import ru.nubby.playstream.domain.entity.StreamListNavigationState;
import ru.nubby.playstream.presentation.base.BaseRxPresenter;

import static ru.nubby.playstream.presentation.streamlist.streamlistfragment.StreamListContract.View.ErrorMessage.ERROR_BAD_CONNECTION;


public class StreamListPresenter extends BaseRxPresenter<StreamListContract.View>
        implements StreamListContract.Presenter {

    private static final String TAG = StreamListPresenter.class.getSimpleName();
    private final long UPDATE_INTERVAL_MILLIS = 1000 * 60 * 5; // 5 minutes

    private Disposable mDisposableFetchingTask;

    private Pagination mPagination;
    private Repository mRepository;

    private List<Stream> mCurrentStreamList;
    private Map<String, Stream> mCurrentStreamMap;
    private Observable<StreamListNavigationState> mListStateObservable;
    private StreamListNavigationState mCurrentState;
    private boolean mForceReload = false;

    public StreamListPresenter(Repository repository) {
        this.mRepository = repository;
        this.mListStateObservable = repository.getObservableNavigationState();
    }

    @Override
    public void subscribe(StreamListContract.View view, Lifecycle lifecycle, long interval) {
        super.subscribe(view, lifecycle);
        mView.setPreviewSize(mRepository.getSharedPreferences().getPreviewSize());

        mForceReload = (interval >= UPDATE_INTERVAL_MILLIS ||
                interval == 0 ||
                mCurrentStreamList == null ||
                mCurrentStreamList.isEmpty());

        mCurrentState = mRepository.getCurrentNavigationState();
        Disposable disposableListState = mListStateObservable
                .subscribe(streamListNavigationState -> {
                    mCurrentState = streamListNavigationState;
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
        if (mCurrentState == StreamListNavigationState.MODE_TOP) {
            getTopStreams();
        } else if (mCurrentState == StreamListNavigationState.MODE_FAVOURITES) {
            getFollowedStreams();
        }
    }

    @Override
    public void getFollowedStreams() {
        if (mDisposableFetchingTask != null) {
            mCompositeDisposable.remove(mDisposableFetchingTask);
            mDisposableFetchingTask.dispose();
        }

        mDisposableFetchingTask = mRepository
                .getLiveStreamsFollowedByUser()
                .doOnSubscribe(disposable -> {
                    Log.d(TAG, "getFollowedStreams do on sub: " + this.toString());
                    if (!disposable.isDisposed()) {
                        mView.clearStreamList();
                        mCurrentStreamList = new ArrayList<>();
                        mCurrentStreamMap = new HashMap<>();
                        mView.setupProgressBar(true);
                    }
                })
                .subscribe(streams -> {
                            checkAndAddStreams(streams);
                            mView.displayStreamList(mCurrentStreamList);
                            mView.setupProgressBar(false);
                            mPagination = null;
                        },
                        error -> {
                            mView.setupProgressBar(false);
                            mView.displayError(ERROR_BAD_CONNECTION);
                            Log.e(TAG, "Error while fetching user follows ", error);
                        });
        mCompositeDisposable.add(mDisposableFetchingTask);
    }

    @Override
    public void getTopStreams() {
        mDisposableFetchingTask = mRepository
                .getTopStreams()
                .doOnSubscribe(disposable -> {
                    Log.d(TAG, "getTopStreams: do on sub " + this.toString());
                    if (!disposable.isDisposed()) {
                        mView.clearStreamList();
                        mCurrentStreamList = new ArrayList<>();
                        mCurrentStreamMap = new HashMap<>();
                        mView.setupProgressBar(true);
                    }
                })
                .subscribe(streams -> {
                            mView.setupProgressBar(false);
                            checkAndAddStreams(streams.getData());
                            mView.displayStreamList(mCurrentStreamList);
                            mPagination = streams.getPagination();
                        },
                        e -> {
                            mView.setupProgressBar(false);
                            mView.displayError(ERROR_BAD_CONNECTION);
                            Log.e(TAG, "Error while fetching streams", e);
                        });
        mCompositeDisposable.add(mDisposableFetchingTask);
    }

    @Override
    public void getMoreStreams() {
        if (mCurrentState == StreamListNavigationState.MODE_TOP && mPagination != null) {
            if (mDisposableFetchingTask != null) {
                mCompositeDisposable.remove(mDisposableFetchingTask);
                mDisposableFetchingTask.dispose();
            }
            mDisposableFetchingTask = mRepository
                    .getTopStreams(mPagination)
                    .subscribe(streams -> {
                                List<Stream> moreStreams = checkAndAddStreams(streams.getData());
                                mView.addStreamList(moreStreams);
                                mPagination = streams.getPagination();
                            },
                            e -> {
                                mView.displayError(ERROR_BAD_CONNECTION);
                                Log.e(TAG, "Error while fetching more streams", e);
                            });
            mCompositeDisposable.add(mDisposableFetchingTask);
        } else {
            Log.e(TAG, "Wrong mode to fetch more streams, pagination cursor = " +
                    mPagination + ", state = " + mCurrentState);
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
