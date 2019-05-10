package ru.nubby.playstream.presentation.stream.player;

import android.util.Log;

import javax.inject.Inject;

import androidx.lifecycle.Lifecycle;
import io.reactivex.disposables.Disposable;
import ru.nubby.playstream.domain.entities.Quality;
import ru.nubby.playstream.domain.entities.QualityLinks;
import ru.nubby.playstream.domain.entities.Stream;
import ru.nubby.playstream.domain.entities.UserData;
import ru.nubby.playstream.domain.interactors.FollowsInteractor;
import ru.nubby.playstream.domain.interactors.PreferencesInteractor;
import ru.nubby.playstream.domain.interactors.StreamsInteractor;
import ru.nubby.playstream.presentation.base.BaseRxPresenter;
import ru.nubby.playstream.utils.RxSchedulersProvider;

import static ru.nubby.playstream.presentation.stream.player.StreamContract.View.InfoMessage.ERROR_CHANNEL_FOLLOW_UNFOLLOW;
import static ru.nubby.playstream.presentation.stream.player.StreamContract.View.InfoMessage.ERROR_FETCHING_ADDITIONAL_INFO;
import static ru.nubby.playstream.presentation.stream.player.StreamContract.View.InfoMessage.INFO_CHANNEL_FOLLOWED;
import static ru.nubby.playstream.presentation.stream.player.StreamContract.View.InfoMessage.INFO_CHANNEL_UNFOLLOWED;

public class StreamPresenter extends BaseRxPresenter<StreamContract.View>
        implements StreamContract.Presenter {

    private final String TAG = StreamPresenter.class.getSimpleName();

    private Disposable mStreamResolutionsInfoTask;
    private Disposable mStreamInfoUpdater;
    private QualityLinks mQualityUrls;

    private Stream mCurrentStream;

    private final FollowsInteractor mFollowsInteractor;
    private final StreamsInteractor mStreamsInteractor;
    private final PreferencesInteractor mPreferencesInteractor;

    private final RxSchedulersProvider mRxSchedulersProvider;

    @Inject
    public StreamPresenter(FollowsInteractor followsInteractor,
                           StreamsInteractor streamsInteractor,
                           PreferencesInteractor preferencesInteractor,
                           RxSchedulersProvider rxSchedulersProvider) {
        mFollowsInteractor = followsInteractor;
        mPreferencesInteractor = preferencesInteractor;
        mStreamsInteractor = streamsInteractor;
        mRxSchedulersProvider = rxSchedulersProvider;
    }

    @Override
    public void subscribe(StreamContract.View view, Lifecycle lifecycle, Stream stream) {
        super.subscribe(view, lifecycle);

        mCurrentStream = stream;
        playStream(stream);

        mView.displayTitle(stream.getTitle());
        mView.displayViewerCount(stream.getViewerCount());

        Disposable followDisplayTask = mFollowsInteractor
                .isUserFollowed(stream.getUserData())
                .observeOn(mRxSchedulersProvider.getUiScheduler())
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
        mCompositeDisposable.add(followDisplayTask);
    }

    @Override
    public void unsubscribe() {
        mCurrentStream = null;
    }

    @Override
    public void playChosenQuality(Quality quality) {
        String url;
        if (mQualityUrls == null) {
            mView.displayInfoMessage(ERROR_FETCHING_ADDITIONAL_INFO,
                    mCurrentStream.getStreamerName());
            Log.e(TAG, "No quality list found");
            return;
        } else {
            url = mQualityUrls.getUrlForQualityOrClosest(quality);
        }

        if (url != null) {
            mView.displayStream(url);
        } else {
            mView.displayInfoMessage(ERROR_FETCHING_ADDITIONAL_INFO,
                    mCurrentStream.getStreamerName());
            Log.e(TAG, "Url not found for chosen quality " + quality);
        }
    }

    @Override
    public void followOrUnfollowChannel() {
        if (mCurrentStream != null) {
            UserData streamer = mCurrentStream.getUserData();
            Disposable followUnfollowTask = mFollowsInteractor
                    .isUserFollowed(streamer)
                    .flatMapCompletable(result -> {
                        if (result) {
                            return mFollowsInteractor.unfollowUser(streamer);
                        } else {
                            return mFollowsInteractor.followUser(streamer);
                        }
                    })
                    .andThen(mFollowsInteractor.isUserFollowed(streamer))
                    .observeOn(mRxSchedulersProvider.getUiScheduler())
                    .subscribe(
                            followStatus -> {
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
            mCompositeDisposable.add(followUnfollowTask);
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

        mStreamResolutionsInfoTask = mStreamsInteractor
                .getStreamLinks(stream)
                .observeOn(mRxSchedulersProvider.getUiScheduler())
                .doOnSubscribe((x) -> mView.displayLoading(true))
                .doFinally(() -> mView.displayLoading(false))
                .subscribe(
                        qualityLinks -> {
                            mQualityUrls = qualityLinks;
                            mView.setQualitiesMenu(qualityLinks.getSortedQualities());
                            Quality defaultQuality = mPreferencesInteractor.getDefaultQuality();
                            String url = qualityLinks.getUrlForQualityOrClosest(defaultQuality);
                            if (url != null) {
                                mView.displayStream(url);
                            } else {
                                mView.displayInfoMessage(ERROR_FETCHING_ADDITIONAL_INFO,
                                        mCurrentStream.getStreamerName());
                            }
                        },
                        error -> {
                            mView.displayInfoMessage(ERROR_FETCHING_ADDITIONAL_INFO,
                                    mCurrentStream.getStreamerName());
                            Log.e(TAG, "Error while fetching quality urls " + error, error);
                        });
        mCompositeDisposable.add(mStreamResolutionsInfoTask);

        mStreamInfoUpdater = mStreamsInteractor
                .getUpdatableStreamInfo(stream)
                .observeOn(mRxSchedulersProvider.getUiScheduler())
                .subscribe(
                        streamUpdated -> {
                            mView.displayTitle(streamUpdated.getTitle());
                            mView.displayViewerCount(streamUpdated.getViewerCount());
                        },
                        error -> Log.e(TAG, "Error while updating stream info. "
                                + error.getMessage(), error));

        mCompositeDisposable.add(mStreamInfoUpdater);

    }

}
