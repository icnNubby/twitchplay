package ru.nubby.playstream.ui.stream.streamplayer;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import ru.nubby.playstream.model.Stream;
import ru.nubby.playstream.twitchapi.RemoteStreamFullInfo;
import ru.nubby.playstream.utils.Quality;

public class StreamPresenter implements StreamContract.Presenter {
    private final String TAG = "StreamPresenter";

    private StreamContract.View mStreamView;
    private Single<Stream> mSingleStream;
    private Disposable mDisposableStreamResolutionsInfo;
    private Disposable mDisposableStreamAdditionalInfo;
    private Disposable mDisposableStreamInfoUpdater;
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
                            mStreamView.displayTitle(streamReturned.getTitle());
                            mStreamView.displayViewerCount(streamReturned.getViewerCount());
                        },
                        error -> Log.e(TAG, "Error while fetching additional data ", error));
    }

    @Override
    public void unsubscribe() {
        if (mDisposableStreamResolutionsInfo != null && !mDisposableStreamResolutionsInfo.isDisposed()) {
            mDisposableStreamResolutionsInfo.dispose();
        }
        if (mDisposableStreamAdditionalInfo != null && !mDisposableStreamAdditionalInfo.isDisposed()) {
            mDisposableStreamAdditionalInfo.dispose();
        }
        if (mDisposableStreamInfoUpdater != null && !mDisposableStreamInfoUpdater.isDisposed()) {
            mDisposableStreamInfoUpdater.dispose();
        }
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

        if (mDisposableStreamInfoUpdater != null && !mDisposableStreamInfoUpdater.isDisposed()) {
            mDisposableStreamInfoUpdater.dispose();
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
                        error -> Log.e(TAG, "Error while fetching quality urls " + error, error));

        mDisposableStreamInfoUpdater = info
                .updateStream(stream)
                .subscribe(streamUpdated -> {
                            mStreamView.displayTitle(streamUpdated.getTitle());
                            mStreamView.displayViewerCount(streamUpdated.getViewerCount());
                        },
                        error -> Log.e(TAG, "Error while updating stream info "
                                + error.getMessage(), error));

        //todo error processing in view
    }

    @Override
    public void volumeTune() {

    }
}
