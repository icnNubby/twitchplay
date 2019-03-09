package ru.nubby.playstream.presentation.streamlist.streamlistfragment;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import io.reactivex.disposables.Disposable;
import ru.nubby.playstream.domain.Repository;
import ru.nubby.playstream.model.Pagination;
import ru.nubby.playstream.model.Stream;
import ru.nubby.playstream.presentation.streamlist.StreamListNavigationState;

import static ru.nubby.playstream.presentation.streamlist.streamlistfragment.StreamListContract.View.ErrorMessage.ERROR_BAD_CONNECTION;

public class StreamListPresenter implements StreamListContract.Presenter {

    private static final String TAG = StreamListPresenter.class.getSimpleName();
    private final long UPDATE_INTERVAL_MILLIS = 1000 * 60 * 5; // 5 minutes

    private StreamListContract.View mStreamListView;
    private Disposable mDisposableFetchingTask;
    private Pagination mPagination;
    private Repository mRepository;

    private List<Stream> mCurrentStreamList;
    private Map<String, Stream> mCurrentStreamMap;
    private StreamListNavigationState mListState;
    private boolean mForceReload;

    @Inject
    public StreamListPresenter(@Nullable StreamListNavigationState state,
                               boolean isFirstLoad,
                               Repository repository) {
        this.mRepository = repository;
        this.mListState = state;
        this.mForceReload = isFirstLoad;
    }

    @Override
    public void subscribe(StreamListContract.View view) {
        mStreamListView = view;
        mStreamListView.setPreviewSize(mRepository.getSharedPreferences().getPreviewSize());
        if (mForceReload && (mDisposableFetchingTask == null || mPagination == null)) {
            mForceReload = false;
            updateStreams();
        } else {
            mStreamListView.displayStreamList(mCurrentStreamList);
        }
    }

    @Override
    public void unsubscribe() {
        if (mDisposableFetchingTask != null) mDisposableFetchingTask.dispose();
        mStreamListView = null;
    }

    @Override
    public void updateStreams() {
        if (mDisposableFetchingTask != null) mDisposableFetchingTask.dispose();
        if (mListState == StreamListNavigationState.MODE_TOP) {
            getTopStreams();
        } else {
            getFollowedStreams();
        }
    }

    @Override
    public void getFollowedStreams() {
        mListState = StreamListNavigationState.MODE_FAVOURITES;
        if (mDisposableFetchingTask != null) mDisposableFetchingTask.dispose();

        mDisposableFetchingTask = mRepository
                .getLiveStreamsFollowedByUser()
                .doOnSubscribe(disposable -> {
                    mStreamListView.clearStreamList();
                    mCurrentStreamList = new ArrayList<>();
                    mCurrentStreamMap = new HashMap<>();
                    mStreamListView.setupProgressBar(true);
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
        mListState = StreamListNavigationState.MODE_TOP;
        mDisposableFetchingTask = mRepository
                .getTopStreams()
                .doOnSubscribe(disposable -> {
                    mStreamListView.clearStreamList();
                    mCurrentStreamList = new ArrayList<>();
                    mCurrentStreamMap = new HashMap<>();
                    mStreamListView.setupProgressBar(true);
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
        if (mListState == StreamListNavigationState.MODE_TOP && mPagination != null) {
            if (mDisposableFetchingTask != null) {
                mDisposableFetchingTask.dispose();
            }
            mDisposableFetchingTask = mRepository
                    .getTopStreams(mPagination)
                    .subscribe(streams -> {
                                List<Stream> added = checkAndAddStreams(streams.getData());
                                mStreamListView.addStreamList(added);
                                mPagination = streams.getPagination();
                            },
                            e -> {
                                mStreamListView.displayError(ERROR_BAD_CONNECTION);
                                Log.e(TAG, "Error while fetching more streams", e);
                            });
        } else {
            Log.e(TAG, "Wrong mode to fetch more streams, pagination cursor = " +
                    mPagination + ", state = " + mListState);
        }
    }

    @Override
    public void decideToReload(long interval) {
        if (interval >= UPDATE_INTERVAL_MILLIS) {
            if (mListState == StreamListNavigationState.MODE_TOP) {
                getTopStreams();
            } else if (mListState == StreamListNavigationState.MODE_FAVOURITES) {
                getFollowedStreams();
            }
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
