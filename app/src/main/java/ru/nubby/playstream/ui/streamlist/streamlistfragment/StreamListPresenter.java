package ru.nubby.playstream.ui.streamlist.streamlistfragment;

import android.util.Log;

import io.reactivex.disposables.Disposable;
import ru.nubby.playstream.model.Pagination;
import ru.nubby.playstream.model.Stream;
import ru.nubby.playstream.twitchapi.RemoteStreamList;
import ru.nubby.playstream.twitchapi.Repository;

public class StreamListPresenter implements StreamListContract.Presenter {
    private static final String TAG = "StreamListPresenter";

    private StreamListContract.View mStreamListView;
    private Disposable mDisposableFetchingTask;
    private Pagination mPagination;
    private Repository mRemoteRepo;

    public StreamListPresenter(StreamListContract.View streamListView) {
        this.mStreamListView = streamListView;
        mStreamListView.setPresenter(this);
        mRemoteRepo = new RemoteStreamList(); //TODO INJECT
    }

    @Override
    public void addMoreStreams() {
        mDisposableFetchingTask = mRemoteRepo
                .getStreams(mPagination)
                .subscribe(streams -> {
                            mStreamListView.addStreamList(streams.getData());
                            mPagination = streams.getPagination();
                        },
                        e -> Log.e(TAG, "Error while fetching more streams", e));
    }

    @Override
    public void updateStreams() {
        mDisposableFetchingTask = mRemoteRepo
                .getStreams()
                .subscribe(streams -> {
                            mStreamListView.displayStreamList(streams.getData());
                            mPagination = streams.getPagination();
                        },
                        e -> Log.e(TAG, "Error while fetching streams", e));
    }

    @Override
    public void showStream(Stream stream) {

    }

    @Override
    public void subscribe() {
        if (mDisposableFetchingTask == null || mPagination == null) {
            updateStreams();
        } else {
            addMoreStreams();
        }
    }

    @Override
    public void unsubscribe() {
        if (mDisposableFetchingTask != null) mDisposableFetchingTask.dispose();
    }
}
