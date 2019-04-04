package ru.nubby.playstream.presentation.stream.streamplayer;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.inject.Inject;

import androidx.lifecycle.Lifecycle;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import ru.nubby.playstream.data.Repository;
import ru.nubby.playstream.domain.entity.Quality;
import ru.nubby.playstream.domain.entity.Stream;
import ru.nubby.playstream.presentation.base.BaseRxPresenter;

import static ru.nubby.playstream.presentation.stream.streamplayer.StreamContract.View.InfoMessage.ERROR_CHANNEL_FOLLOW_UNFOLLOW;
import static ru.nubby.playstream.presentation.stream.streamplayer.StreamContract.View.InfoMessage.ERROR_FETCHING_ADDITIONAL_INFO;
import static ru.nubby.playstream.presentation.stream.streamplayer.StreamContract.View.InfoMessage.INFO_CHANNEL_FOLLOWED;
import static ru.nubby.playstream.presentation.stream.streamplayer.StreamContract.View.InfoMessage.INFO_CHANNEL_UNFOLLOWED;

public class StreamPresenter extends BaseRxPresenter<StreamContract.View>
        implements StreamContract.Presenter {
    private final String TAG = "StreamPresenter";

    private Disposable mStreamResolutionsInfoTask;
    private Disposable mStreamAdditionalInfoTask;
    private Disposable mStreamInfoUpdater;
    private Disposable mFollowUnfollowTask;
    private Disposable mFollowDisplayTask;
    private HashMap<Quality, String> mQualityUrls;
    private ArrayList<Quality> mQualities;

    private Stream mCurrentStream;

    private Repository mRepository;

    @Inject
    public StreamPresenter(Repository repository) {
        mRepository = repository;
    }

    @Override
    public void subscribe(StreamContract.View view, Lifecycle lifecycle, Stream stream) {
        super.subscribe(view, lifecycle);

        Stream streamCopy = new Stream(stream);
        Single<Stream> initialStreamRequest = mRepository
                .getUserFromStreamer(stream)
                .map(updatedLogin -> {
                    streamCopy.setStreamerLogin(updatedLogin.getLogin());
                    return streamCopy;
                });

        mStreamAdditionalInfoTask = initialStreamRequest
                .doOnSubscribe(streamReturned -> mView.displayLoading(true))
                .subscribe(
                        streamReturned -> {
                            mCurrentStream = streamReturned;
                            playStream(streamReturned);
                            mView.displayLoading(false);
                            mView.displayTitle(streamReturned.getTitle());
                            mView.displayViewerCount(streamReturned.getViewerCount());
                            mFollowDisplayTask = mRepository
                                    .isStreamFollowed(streamReturned)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(
                                            followStatus -> {
                                                mView.displayFollowStatus(followStatus);
                                                mView.enableFollow(true);
                                            },
                                            error -> {
                                                Log.e(TAG, "Follow error " + error, error);
                                                mView.enableFollow(false);
                                                mView.displayInfoMessage(
                                                        ERROR_CHANNEL_FOLLOW_UNFOLLOW,
                                                        mCurrentStream.getStreamerName());
                                            }
                                    );
                            mCompositeDisposable.add(mFollowDisplayTask);
                        },
                        error -> {
                            mCurrentStream = null;
                            mView.displayLoading(false);
                            mView.enableFollow(false);
                            Log.e(TAG, "Error while fetching additional data ", error);
                        });
        mCompositeDisposable.add(mStreamAdditionalInfoTask);
    }

    @Override
    public void unsubscribe() {
        mCurrentStream = null;
    }

    @Override
    public void playChosenQuality(Quality quality) {
        String url = mQualityUrls.get(quality);
        mView.displayStream(url);
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
                    .subscribe(followStatus -> {
                                mView.displayFollowStatus(followStatus);
                                mView.enableFollow(true);
                                if (followStatus) {
                                    mView.displayInfoMessage(INFO_CHANNEL_FOLLOWED,
                                            mCurrentStream.getStreamerName());
                                } else {
                                    mView.displayInfoMessage(INFO_CHANNEL_UNFOLLOWED,
                                            mCurrentStream.getStreamerName());
                                }
                            },
                            error -> {
                                Log.e(TAG, "Follow error " + error, error);
                                mView.enableFollow(false);
                                mView.displayInfoMessage(ERROR_CHANNEL_FOLLOW_UNFOLLOW,
                                        mCurrentStream.getStreamerName());
                            });
            mCompositeDisposable.add(mFollowUnfollowTask);
        } else {
            mView.enableFollow(false);
        }
    }

    private void playStream(Stream stream) {

        if (mStreamResolutionsInfoTask != null && !mStreamResolutionsInfoTask.isDisposed()) {
            mStreamResolutionsInfoTask.dispose();
        }

        if (mStreamInfoUpdater != null && !mStreamInfoUpdater.isDisposed()) {
            mStreamInfoUpdater.dispose();
        }

        mView.displayLoading(true);
        mStreamResolutionsInfoTask = mRepository
                .getQualityUrls(stream)
                .subscribe(fetchedQualityTable -> {
                            mQualityUrls = fetchedQualityTable;
                            //set available qualities to menu, sorted
                            mQualities = new ArrayList<>(mQualityUrls.keySet());
                            Collections.sort(mQualities);
                            mView.setQualitiesMenu(mQualities);

                            //get url for default or if not exists for closest better quality.
                            Quality defaultQuality = mRepository
                                            .getSharedPreferences()
                                            .getDefaultQuality();
                            Quality nextQuality = defaultQuality;
                            String url = mQualityUrls.get(defaultQuality);
                            while (url == null && nextQuality.ordinal() > 0) {
                                nextQuality = Quality.values()[nextQuality.ordinal() - 1];
                                url = mQualityUrls.get(nextQuality);
                            }
                            if (!mQualities.isEmpty() || url == null) {
                                mView.displayStream(url);
                            } else {
                                mView.displayInfoMessage(ERROR_FETCHING_ADDITIONAL_INFO,
                                        mCurrentStream.getStreamerName());
                            }
                            mView.displayLoading(false);
                        },
                        error -> {
                            mView.displayInfoMessage(ERROR_FETCHING_ADDITIONAL_INFO,
                                    mCurrentStream.getStreamerName());
                            mView.displayLoading(false);
                            Log.e(TAG, "Error while fetching quality urls " + error, error);
                        });
        mCompositeDisposable.add(mStreamResolutionsInfoTask);

        mStreamInfoUpdater = mRepository
                .getUpdatableStreamInfo(stream)
                .subscribe(streamUpdated -> {
                            mView.displayTitle(streamUpdated.getTitle());
                            mView.displayViewerCount(streamUpdated.getViewerCount());
                        },
                        error -> Log.e(TAG, "Error while updating stream info. "
                                + error.getMessage(), error));

        mCompositeDisposable.add(mStreamInfoUpdater);

    }

}
