package ru.nubby.playstream.presentation.streamlist.streamlistfragment;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModel;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import ru.nubby.playstream.data.Repository;
import ru.nubby.playstream.model.Pagination;
import ru.nubby.playstream.model.Stream;
import ru.nubby.playstream.model.StreamListNavigationState;
import ru.nubby.playstream.presentation.BasePresenterImpl;

import static ru.nubby.playstream.presentation.streamlist.streamlistfragment.StreamListContract.View.ErrorMessage.ERROR_BAD_CONNECTION;


public class StreamListPresenter extends BasePresenterImpl<StreamListContract.View>
    implements StreamListContract.Presenter{

    private static final String TAG = StreamListPresenter.class.getSimpleName();
    private final long UPDATE_INTERVAL_MILLIS = 1000 * 60 * 5; // 5 minutes

    private StreamListContract.View mStreamListView;
    private Disposable mDisposableFetchingTask;
    private Disposable mDisposableListState;

    private Pagination mPagination;
    private Repository mRepository;

    private List<Stream> mCurrentStreamList;
    private Map<String, Stream> mCurrentStreamMap;
    private Observable<StreamListNavigationState> mListStateObservable;
    private StreamListNavigationState mCurrentState;
    private boolean mForceReload = false;

    @Inject
    public StreamListPresenter(Repository repository) {
        this.mRepository = repository;
        this.mListStateObservable = repository.getObservableNavigationState();
    }

    public void subscribe(StreamListContract.View view, Lifecycle lifecycle, long interval) {
        super.subscribe(view, lifecycle);
        mStreamListView = view;
        mStreamListView.setPreviewSize(mRepository.getSharedPreferences().getPreviewSize());

        mForceReload = (interval >= UPDATE_INTERVAL_MILLIS ||
                interval == 0);

        mCurrentState = mRepository.getCurrentNavigationState();
        mDisposableListState = mListStateObservable
                .subscribe(streamListNavigationState -> {
                    mCurrentState = streamListNavigationState;
                    if (mForceReload) {
                        updateStreams();
                    } else {
                        mForceReload = true;
                        mStreamListView.displayStreamList(mCurrentStreamList);
                    }
                });
    }

    @Override
    public void unsubscribe() {
        Log.d(TAG, "unsubscribe: " + this.toString());
        if (mDisposableFetchingTask != null) {
            mDisposableFetchingTask.dispose();
        }
        if (mDisposableListState != null) {
            mDisposableListState.dispose();
        }
        Log.d(TAG, "unsubscribe: all disposed, view cleared " + this.toString());
        mStreamListView = null;
    }

    @Override
    public void updateStreams() {
        if (mDisposableFetchingTask != null) {
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
        if (mDisposableFetchingTask != null) mDisposableFetchingTask.dispose();

        mDisposableFetchingTask = mRepository
                .getLiveStreamsFollowedByUser()
                .doOnSubscribe(disposable -> {
                    Log.d(TAG, "getFollowedStreams do on sub: " + this.toString());
                    if (!disposable.isDisposed()) {
                        mStreamListView.clearStreamList();
                        mCurrentStreamList = new ArrayList<>();
                        mCurrentStreamMap = new HashMap<>();
                        mStreamListView.setupProgressBar(true);
                    }
                })
                .subscribe(streams -> {
                            checkAndAddStreams(streams);
                            mStreamListView.displayStreamList(mCurrentStreamList);
                            mStreamListView.setupProgressBar(false);
                            mPagination = null;
                        },
                        error -> {
                            mStreamListView.setupProgressBar(false);
                            mStreamListView.displayError(ERROR_BAD_CONNECTION);
                            Log.e(TAG, "Error while fetching user follows ", error);
                        });
    }

    @Override
    public void getTopStreams() {
        mDisposableFetchingTask = mRepository
                .getTopStreams()
                .doOnSubscribe(disposable -> {
                    Log.d(TAG, "getTopStreams: do on sub " + this.toString());
                    if (!disposable.isDisposed()) {
                        mStreamListView.clearStreamList();
                        mCurrentStreamList = new ArrayList<>();
                        mCurrentStreamMap = new HashMap<>();
                        mStreamListView.setupProgressBar(true);
                    }
                })
                .subscribe(streams -> {
                            mStreamListView.setupProgressBar(false);
                            checkAndAddStreams(streams.getData());
                            mStreamListView.displayStreamList(mCurrentStreamList);
                            mPagination = streams.getPagination();
                        },
                        e -> {
                            mStreamListView.setupProgressBar(false);
                            mStreamListView.displayError(ERROR_BAD_CONNECTION);
                            Log.e(TAG, "Error while fetching streams", e);
                        });
    }

    @Override
    public void getMoreStreams() {
        if (mCurrentState == StreamListNavigationState.MODE_TOP && mPagination != null) {
            if (mDisposableFetchingTask != null) {
                mDisposableFetchingTask.dispose();
            }
            mDisposableFetchingTask = mRepository
                    .getTopStreams(mPagination)
                    .subscribe(streams -> {
                                List<Stream> moreStreams = checkAndAddStreams(streams.getData());
                                mStreamListView.addStreamList(moreStreams);
                                mPagination = streams.getPagination();
                            },
                            e -> {
                                mStreamListView.displayError(ERROR_BAD_CONNECTION);
                                Log.e(TAG, "Error while fetching more streams", e);
                            });
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
