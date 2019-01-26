package ru.nubby.playstream.streamlist;

import android.util.Log;

import io.reactivex.disposables.Disposable;
import ru.nubby.playstream.model.Stream;
import ru.nubby.playstream.net.RemoteStreamList;
import ru.nubby.playstream.net.Repository;

public class StreamListPresenter implements StreamListContract.Presenter {
    private StreamListContract.View mStreamListView;
    private Disposable mDisposable;

    public StreamListPresenter(StreamListContract.View streamListView) {
        this.mStreamListView = streamListView;
        mStreamListView.setPresenter(this);
    }

    @Override
    public void addMoreStreams() {
        Repository internet = new RemoteStreamList(); //TODO INJECT
        mDisposable = internet.getStreams()
                .subscribe(x -> mStreamListView.displayStreamList(x),
                        e -> Log.e("STREAM PRESENTER", "Error while fetching streams", e));
    }

    @Override
    public void updateStreams() {
            //TODO
    }

    @Override
    public void showStream(Stream stream) {

    }

    @Override
    public void subscribe() {
            //TODO
    }

    @Override
    public void unsubscribe() {
        if (mDisposable != null) mDisposable.dispose();
    }
}
