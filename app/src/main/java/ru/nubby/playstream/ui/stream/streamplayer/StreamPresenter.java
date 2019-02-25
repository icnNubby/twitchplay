package ru.nubby.playstream.ui.stream.streamplayer;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import ru.nubby.playstream.data.Repository;
import ru.nubby.playstream.model.Quality;
import ru.nubby.playstream.model.Stream;

import static ru.nubby.playstream.ui.stream.streamplayer.StreamContract.View.InfoMessage.ERROR_CHANNEL_FOLLOW_UNFOLLOW;
import static ru.nubby.playstream.ui.stream.streamplayer.StreamContract.View.InfoMessage.ERROR_FETCHING_ADDITIONAL_INFO;
import static ru.nubby.playstream.ui.stream.streamplayer.StreamContract.View.InfoMessage.INFO_CHANNEL_FOLLOWED;
import static ru.nubby.playstream.ui.stream.streamplayer.StreamContract.View.InfoMessage.INFO_CHANNEL_UNFOLLOWED;

public class StreamPresenter implements StreamContract.Presenter {
    private final String TAG = "StreamPresenter";

    private StreamContract.View mStreamView;
    private Single<Stream> mStreamRequest;
    private Disposable mStreamResolutionsInfoTask;
    private Disposable mStreamAdditionalInfoTask;
    private Disposable mStreamInfoUpdater;
    private Disposable mFollowUnfollowTask;
    private Disposable mFollowDisplayTask;
    private HashMap<Quality, String> mQualityUrls;
    private ArrayList<Quality> mQualities;

    private Stream mCurrentStream;

    private Repository mRepository; //TODO inject

    public StreamPresenter(StreamContract.View streamView, Single<Stream> stream, Repository repository) {
        this.mStreamView = streamView;
        mStreamRequest = stream;
        streamView.setPresenter(this);
        mRepository = repository;
    }

    @Override
    public void subscribe() {
        mStreamAdditionalInfoTask = mStreamRequest
                .doOnSubscribe(streamReturned -> mStreamView.displayLoading(true))
                .subscribe(streamReturned -> {
                            mCurrentStream = streamReturned;
                            playStream(streamReturned);
                            mStreamView.displayLoading(false);
                            mStreamView.displayTitle(streamReturned.getTitle());
                            mStreamView.displayViewerCount(streamReturned.getViewerCount());
                            mFollowDisplayTask = mRepository
                                    .isStreamFollowed(streamReturned)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(
                                            success -> {
                                                mStreamView.displayFollowStatus(success);
                                                mStreamView.enableFollow(true);
                                            },
                                            error -> {
                                                Log.e(TAG, "Follow error " + error, error);
                                                mStreamView.enableFollow(false);
                                                mStreamView.displayInfoMessage(
                                                        ERROR_CHANNEL_FOLLOW_UNFOLLOW,
                                                        mCurrentStream.getStreamerName());
                                            }
                                    );
                        },
                        error -> {
                            mCurrentStream = null;
                            mStreamView.displayLoading(false);
                            mStreamView.enableFollow(false);
                            Log.e(TAG, "Error while fetching additional data ", error);
                        });
    }

    @Override
    public void unsubscribe() {
        if (mStreamResolutionsInfoTask != null && !mStreamResolutionsInfoTask.isDisposed()) {
            mStreamResolutionsInfoTask.dispose();
        }
        if (mStreamAdditionalInfoTask != null && !mStreamAdditionalInfoTask.isDisposed()) {
            mStreamAdditionalInfoTask.dispose();
        }
        if (mStreamInfoUpdater != null && !mStreamInfoUpdater.isDisposed()) {
            mStreamInfoUpdater.dispose();
        }
        if (mFollowUnfollowTask != null && !mFollowUnfollowTask.isDisposed()) {
            mFollowUnfollowTask.dispose();
        }
        if (mFollowDisplayTask != null && !mFollowDisplayTask.isDisposed()) {
            mFollowDisplayTask.dispose();
        }

        mCurrentStream = null;
    }

    @Override
    public void playChosenQuality(Quality quality) {
        String url = mQualityUrls.get(quality);
        mStreamView.displayStream(url);
    }

    @Override
    public void followOrUnfollowChannel() {
        if (mCurrentStream != null) {
            mFollowUnfollowTask = mRepository
                    .isStreamFollowed(mCurrentStream)
                    .subscribeOn(Schedulers.io())
                    .flatMapCompletable(result -> {
                        if (result) {
                            return mRepository.unfollowStream(mCurrentStream);
                        } else {
                            return mRepository.followStream(mCurrentStream);
                        }
                    })
                    .andThen(mRepository.isStreamFollowed(mCurrentStream))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(result -> {
                                mStreamView.displayFollowStatus(result);
                                mStreamView.enableFollow(true);
                                if (result) {
                                    mStreamView.displayInfoMessage(INFO_CHANNEL_FOLLOWED,
                                            mCurrentStream.getStreamerName());
                                } else {
                                    mStreamView.displayInfoMessage(INFO_CHANNEL_UNFOLLOWED,
                                            mCurrentStream.getStreamerName());
                                }
                            },
                            error -> {
                                Log.e(TAG, "Follow error " + error, error);
                                mStreamView.enableFollow(false);
                                mStreamView.displayInfoMessage(ERROR_CHANNEL_FOLLOW_UNFOLLOW,
                                        mCurrentStream.getStreamerName());
                            });
        } else {
            mStreamView.enableFollow(false);
        }
    }

    private void playStream(Stream stream) {

        if (mStreamResolutionsInfoTask != null && !mStreamResolutionsInfoTask.isDisposed()) {
            mStreamResolutionsInfoTask.dispose();
        }

        if (mStreamInfoUpdater != null && !mStreamInfoUpdater.isDisposed()) {
            mStreamInfoUpdater.dispose();
        }
        mStreamView.displayLoading(true);
        mStreamResolutionsInfoTask = mRepository
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
                                mStreamView.displayInfoMessage(ERROR_FETCHING_ADDITIONAL_INFO,
                                        mCurrentStream.getStreamerName());
                            }
                            mStreamView.displayLoading(false);
                        },
                        error -> {
                            mStreamView.displayInfoMessage(ERROR_FETCHING_ADDITIONAL_INFO,
                                    mCurrentStream.getStreamerName());
                            mStreamView.displayLoading(false);
                            Log.e(TAG, "Error while fetching quality urls " + error, error);
                        });

        mStreamInfoUpdater = mRepository
                .getUpdatedStreamInfo(stream)
                .subscribe(streamUpdated -> {
                            mStreamView.displayTitle(streamUpdated.getTitle());
                            mStreamView.displayViewerCount(streamUpdated.getViewerCount());
                        },
                        error -> Log.e(TAG, "Error while updating stream mRepository "
                                + error.getMessage(), error));

        //todo error processing in view
    }

}
