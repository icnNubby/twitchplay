package ru.nubby.playstream.ui.streamlist.streamlistfragment;

import android.util.Log;

import java.util.List;

import io.reactivex.disposables.Disposable;
import ru.nubby.playstream.model.Pagination;
import ru.nubby.playstream.model.Stream;
import ru.nubby.playstream.twitchapi.RemoteStreamList;
import ru.nubby.playstream.twitchapi.Repository;
import ru.nubby.playstream.ui.streamlist.StreamListNavigationState;
import ru.nubby.playstream.utils.SharedPreferencesHelper;

public class StreamListPresenter implements StreamListContract.Presenter {

    private static final String TAG = "StreamListPresenter";

    private StreamListContract.View mStreamListView;
    private Disposable mDisposableFetchingTask;
    private Pagination mPagination;
    private Repository mRemoteRepo;

    private List<Stream> mCurrentStreamList;
    private StreamListNavigationState mListState;
    private boolean forceReload;

    public StreamListPresenter(StreamListContract.View streamListView, StreamListNavigationState state, boolean forceReload) {
        this.mStreamListView = streamListView;
        mStreamListView.setPresenter(this);
        mRemoteRepo = new RemoteStreamList(); //TODO INJECT
        mListState = state;
        this.forceReload = forceReload;
    }

    @Override
    public void getMoreTopStreams() {
        if (mListState != StreamListNavigationState.MODE_FAVOURITES && mPagination != null) {
            if (mDisposableFetchingTask != null) mDisposableFetchingTask.dispose();
            mDisposableFetchingTask = mRemoteRepo
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
                            e -> Log.e(TAG, "Error while fetching more streams", e));
        }
    }

    @Override
    public void updateStreams() {
        if (mDisposableFetchingTask != null) mDisposableFetchingTask.dispose();

        if (mListState == StreamListNavigationState.MODE_TOP) {
            mDisposableFetchingTask = mRemoteRepo
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
                                Log.e(TAG, "Error while fetching streams", e);
                            });
        } else {
            getFollowedStreams();
        }
    }

    @Override
    public void getFollowedStreams() {
        mListState = StreamListNavigationState.MODE_FAVOURITES;
        if (mDisposableFetchingTask != null) mDisposableFetchingTask.dispose();
        mDisposableFetchingTask = mRemoteRepo
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
                            Log.e(TAG, "Error while fetching user follows ", error);
                        });
    }

    @Override
    public void getTopStreams() {
        mListState = StreamListNavigationState.MODE_TOP;
        updateStreams();
    }

    @Override
    public void subscribe() {
        if (forceReload && (mDisposableFetchingTask == null || mPagination == null)) {
            forceReload = false;
            if (mListState == StreamListNavigationState.MODE_TOP) {
                getTopStreams();
            } else if (mListState == StreamListNavigationState.MODE_FAVOURITES) {
                getFollowedStreams();
            }
        } else {
            mStreamListView.displayNewStreamList(mCurrentStreamList);
        }
    }

    @Override
    public void unsubscribe() {
        if (mDisposableFetchingTask != null) mDisposableFetchingTask.dispose();
    }
}
