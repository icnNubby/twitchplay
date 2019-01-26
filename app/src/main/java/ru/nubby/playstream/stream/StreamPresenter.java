package ru.nubby.playstream.stream;

import android.util.Log;

import io.reactivex.disposables.Disposable;
import ru.nubby.playstream.model.Stream;
import ru.nubby.playstream.net.RemoteStreamFullInfo;

public class StreamPresenter implements StreamContract.Presenter {

    private StreamContract.View mStreamView;
    private Stream mStream;
    private Disposable mDisposable;

    public StreamPresenter(StreamContract.View streamView, Stream stream) {
        this.mStreamView = streamView;
        mStream = stream;
        streamView.setPresenter(this);
        playStream(mStream);
    }
    @Override
    public void subscribe() {
        //TODO
    }

    @Override
    public void unsubscribe() {
        if (mDisposable != null) mDisposable.dispose();
    }

    @Override
    public void pauseStream() {

    }

    @Override
    public void playStream(Stream stream) {
        RemoteStreamFullInfo info = new RemoteStreamFullInfo();
        mDisposable = info.getVideoUrl(stream)
                .subscribe(x -> mStreamView.displayStream(x),
                        e -> Log.e("StreamPresenter", "Error while fetching ", e));
    }

    @Override
    public void volumeTune() {

    }
}
