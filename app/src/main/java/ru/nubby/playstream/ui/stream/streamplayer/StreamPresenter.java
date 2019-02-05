package ru.nubby.playstream.ui.stream.streamplayer;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import androidx.lifecycle.LifecycleObserver;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import ru.nubby.playstream.model.Stream;
import ru.nubby.playstream.twitchapi.RemoteStreamFullInfo;
import ru.nubby.playstream.utils.Quality;

public class StreamPresenter implements StreamContract.Presenter, LifecycleObserver {
    private final String TAG = "StreamPresenter";

    private StreamContract.View mStreamView;
    private Single<Stream> mSingleStream;
    private Disposable mDisposableStreamResolutionsInfo;
    private Disposable mDisposableStreamAdditionalInfo;
    private HashMap<Quality, String> mQualityUrls;
    private ArrayList<Quality> mQualities;

    public StreamPresenter(StreamContract.View streamView, Single<Stream> stream) {
        this.mStreamView = streamView;
        mSingleStream = stream;
        streamView.setPresenter(this);
    }

    @Override
    public void subscribe() {
        mDisposableStreamAdditionalInfo = mSingleStream
                .doOnSubscribe(streamReturned -> mStreamView.displayLoading(true))
                .subscribe(streamReturned -> {
                            playStream(streamReturned);
                            mStreamView.displayLoading(false);
                        },
                        error -> Log.e(TAG, "Error while fetching additional data ", error));
    }

    @Override
    public void unsubscribe() {
        if (!mDisposableStreamResolutionsInfo.isDisposed())
            mDisposableStreamResolutionsInfo.dispose();
        if (!mDisposableStreamAdditionalInfo.isDisposed())
            mDisposableStreamAdditionalInfo.dispose();
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

        if (mDisposableStreamResolutionsInfo != null && !mDisposableStreamResolutionsInfo.isDisposed()) {
            mDisposableStreamResolutionsInfo.dispose();
        }

        RemoteStreamFullInfo info = new RemoteStreamFullInfo();
        mStreamView.displayLoading(true);
        mDisposableStreamResolutionsInfo = info
                .getVideoUrl(stream)
                .subscribe(fetchedQualityTable -> {
                            mQualityUrls = fetchedQualityTable;
                            mQualities = new ArrayList<>(mQualityUrls.keySet());
                            Collections.sort(mQualities);
                            mStreamView.setQualitiesMenu(mQualities);
                            int original = mQualities.indexOf(Quality.QUALITY72030); //TODO get from prefs
                            original = original >= 0 ? original : 0;
                            if (!mQualities.isEmpty()) {
                                mStreamView.displayStream(mQualityUrls.get(mQualities.get(original)));
                            } else {
                                //TODO display error of fetching
                            }
                            mStreamView.displayLoading(false);
                        },
                        e -> Log.e("StreamPresenter", "Error while fetching quality urls " + e, e));
        //todo error processing in view
    }

    @Override
    public void volumeTune() {

    }
}
