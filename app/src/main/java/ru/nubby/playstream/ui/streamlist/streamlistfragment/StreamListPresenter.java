package ru.nubby.playstream.ui.streamlist.streamlistfragment;

import android.util.Log;

import io.reactivex.disposables.Disposable;
import ru.nubby.playstream.model.Pagination;
import ru.nubby.playstream.model.Stream;
import ru.nubby.playstream.twitchapi.RemoteStreamList;
import ru.nubby.playstream.twitchapi.Repository;
import ru.nubby.playstream.utils.SharedPreferencesHelper;

public class StreamListPresenter implements StreamListContract.Presenter {

    private static final String TAG = "StreamListPresenter";
    private static final int STATE_FAVOURITES = 1;
    private static final int STATE_TOP = 2;

    private StreamListContract.View mStreamListView;
    private Disposable mDisposableFetchingTask;
    private Pagination mPagination;
    private Repository mRemoteRepo;

    private int listState;


    public StreamListPresenter(StreamListContract.View streamListView) {
        this.mStreamListView = streamListView;
        mStreamListView.setPresenter(this);
        mRemoteRepo = new RemoteStreamList(); //TODO INJECT
        listState = 2; //TODO prob get from prefs
    }

    @Override
    public void addMoreStreams() {
        if (listState != STATE_FAVOURITES && mPagination != null) {
            if (mDisposableFetchingTask != null) mDisposableFetchingTask.dispose();
            mDisposableFetchingTask = mRemoteRepo
                    .getStreams(mPagination)
                    .subscribe(streams -> {
                                mStreamListView.addStreamList(streams.getData());
                                mPagination = streams.getPagination();
                            },
                            e -> Log.e(TAG, "Error while fetching more streams", e));
        }
    }

    @Override
    public void updateStreams() {
        if (mDisposableFetchingTask != null) mDisposableFetchingTask.dispose();
        if (listState == STATE_TOP) {
            mDisposableFetchingTask = mRemoteRepo
                    .getStreams()
                    .subscribe(streams -> {
                                mStreamListView.displayNewStreamList(streams.getData());
                                mPagination = streams.getPagination();
                            },
                            e -> Log.e(TAG, "Error while fetching streams", e));
        } else {
            getFollowedStreams();
        }
    }

    @Override
    public void showStream(Stream stream) {

    }

    @Override
    public void getFollowedStreams() {
        listState = STATE_FAVOURITES;
        if (mDisposableFetchingTask != null) mDisposableFetchingTask.dispose();
        mDisposableFetchingTask = mRemoteRepo
                .getLiveStreamsFollowedByUser(SharedPreferencesHelper.getUserData().getId())
                .subscribe(streams -> {
                            mStreamListView.displayNewStreamList(streams);
                            mPagination = null;
                        },
                        error -> Log.e(TAG, "Error while fetching user follows ", error));
    }

    @Override
    public void getTopStreams() {
        listState = STATE_TOP;
        updateStreams();
    }

    @Override
    public void subscribe() {
        if (mDisposableFetchingTask == null || mPagination == null) {
            if (listState == STATE_TOP) {
                getTopStreams();
            } else if (listState == STATE_FAVOURITES) {
                getFollowedStreams();
            }
        }
    }

    @Override
    public void unsubscribe() {
        if (mDisposableFetchingTask != null) mDisposableFetchingTask.dispose();
    }
}
