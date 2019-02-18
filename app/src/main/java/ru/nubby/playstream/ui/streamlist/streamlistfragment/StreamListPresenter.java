package ru.nubby.playstream.ui.streamlist.streamlistfragment;

import android.util.Log;

import java.util.List;

import io.reactivex.disposables.Disposable;
import ru.nubby.playstream.model.Pagination;
import ru.nubby.playstream.model.Stream;
import ru.nubby.playstream.data.twitchapi.RemoteStreamList;
import ru.nubby.playstream.data.Repository;
import ru.nubby.playstream.ui.streamlist.StreamListNavigationState;
import ru.nubby.playstream.utils.SharedPreferencesHelper;

import static ru.nubby.playstream.ui.streamlist.streamlistfragment.StreamListContract.View.ErrorMessage.ERROR_BAD_CONNECTION;

public class StreamListPresenter implements StreamListContract.Presenter {

    private static final String TAG = StreamListPresenter.class.getSimpleName();
    private final long UPDATE_INTERVAL_MILLIS = 5 * 60 * 1000; //TODO 5 minutes

    private StreamListContract.View mStreamListView;
    private Disposable mDisposableFetchingTask;
    private Pagination mPagination;
    private Repository mRepository;

    private List<Stream> mCurrentStreamList;
    private StreamListNavigationState mListState;
    private boolean forceReload;

    public StreamListPresenter(StreamListContract.View streamListView,
                               StreamListNavigationState state,
                               boolean forceReload,
                               Repository repository) {
        this.mStreamListView = streamListView;
        mStreamListView.setPresenter(this);
        mRepository = repository; //TODO INJECT
        mListState = state;
        this.forceReload = forceReload;
    }

    @Override
    public void getMoreTopStreams() {
        if (mListState != StreamListNavigationState.MODE_FAVOURITES && mPagination != null) {
            if (mDisposableFetchingTask != null) {
                mDisposableFetchingTask.dispose();
            }
            mDisposableFetchingTask = mRepository
                    .getStreams(mPagination)
                    .subscribe(streams -> {
                                if (mCurrentStreamList != null) {
                                    mCurrentStreamList.addAll(streams.getData());
                                } else {
                                    mCurrentStreamList = streams.getData();
                                }
                                mStreamListView.addStreamList(streams.getData());
                                mPagination = streams.getPagination();
                            },
                            e -> {
                                mStreamListView.displayError(ERROR_BAD_CONNECTION);
                                Log.e(TAG, "Error while fetching more streams", e);
                            });
        } else {
            Log.e(TAG, "Error while fetching more streams, pagination cursor = " +
                    mPagination + ", state = " + mListState);
        }
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
        if (SharedPreferencesHelper.getUserData() != null) {
            mDisposableFetchingTask = mRepository
                    .getLiveStreamsFollowedByUser(SharedPreferencesHelper.getUserData().getId())
                    .doOnSubscribe(disposable -> {
                        mStreamListView.clearStreamList();
                        mStreamListView.setupProgressBar(true);
                    })
                    .subscribe(streams -> {
                                mCurrentStreamList = streams;
                                mStreamListView.displayNewStreamList(streams);
                                mStreamListView.setupProgressBar(false);
                                mPagination = null;
                            },
                            error -> {
                                mStreamListView.setupProgressBar(false);
                                mStreamListView.displayError(ERROR_BAD_CONNECTION);
                                Log.e(TAG, "Error while fetching user follows ", error);
                            });
        }
    }

    @Override
    public void getTopStreams() {
        mListState = StreamListNavigationState.MODE_TOP;
        mDisposableFetchingTask = mRepository
                .getStreams()
                .doOnSubscribe(disposable -> {
                    mStreamListView.clearStreamList();
                    mStreamListView.setupProgressBar(true);
                })
                .subscribe(streams -> {
                            mCurrentStreamList = streams.getData();
                            mStreamListView.displayNewStreamList(streams.getData());
                            mPagination = streams.getPagination();
                            mStreamListView.setupProgressBar(false);
                        },
                        e -> {
                            mStreamListView.setupProgressBar(false);
                            mStreamListView.displayError(ERROR_BAD_CONNECTION);
                            Log.e(TAG, "Error while fetching streams", e);
                        });
    }

    @Override
    public void decideToReload(long interval) {
        if (interval >= UPDATE_INTERVAL_MILLIS) {
            mStreamListView.clearStreamList();
            if (mListState == StreamListNavigationState.MODE_TOP) {
                getTopStreams();
            } else if (mListState == StreamListNavigationState.MODE_FAVOURITES) {
                getFollowedStreams();
            }
        }
    }

    @Override
    public void subscribe() {
        if (forceReload && (mDisposableFetchingTask == null || mPagination == null)) {
            mStreamListView.clearStreamList();
            forceReload = false;
            updateStreams();
        } else {
            mStreamListView.displayNewStreamList(mCurrentStreamList);
        }
    }

    @Override
    public void unsubscribe() {
        if (mDisposableFetchingTask != null) mDisposableFetchingTask.dispose();
    }
}
