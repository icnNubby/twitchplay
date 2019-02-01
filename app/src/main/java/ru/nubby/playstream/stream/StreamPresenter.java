package ru.nubby.playstream.stream;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import io.reactivex.disposables.Disposable;
import ru.nubby.playstream.model.Stream;
import ru.nubby.playstream.twitchapi.RemoteStreamFullInfo;
import ru.nubby.playstream.utils.Quality;

public class StreamPresenter implements StreamContract.Presenter, LifecycleObserver {

    private StreamContract.View mStreamView;
    private Stream mStream;
    private Disposable mDisposable;
    private HashMap<Quality, String> mQualityUrls;
    private ArrayList<Quality> mQualities;

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
    public void playChosenQuality(Quality quality) {
        String url = mQualityUrls.get(quality);
        mStreamView.displayStream(url);
    }

    @Override
    public void pauseStream() {

    }

    @Override
    public void playStream(Stream stream) {
        RemoteStreamFullInfo info = new RemoteStreamFullInfo();
        mDisposable = info.getVideoUrl(stream)
                .subscribe(fetchedQualityTable -> {
                            mQualityUrls = fetchedQualityTable;
                            mQualities = new ArrayList<>(mQualityUrls.keySet());
                            Collections.sort(mQualities);
                            mStreamView.setQualitiesMenu(mQualities);
                            int original = mQualities.indexOf(Quality.QUALITY72030); //TODO get from prefs
                            original = original >= 0? original:0;
                            if (!mQualities.isEmpty()) {
                                mStreamView.displayStream(mQualityUrls.get(mQualities.get(original)));
                            }
                        },
                        e -> Log.e("StreamPresenter", "Error while fetching quality urls " + e, e));
        //todo error prosessiing in view
    }

    @Override
    public void volumeTune() {

    }
}
