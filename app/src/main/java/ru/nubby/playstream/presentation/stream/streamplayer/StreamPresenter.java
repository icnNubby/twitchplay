package ru.nubby.playstream.presentation.stream.streamplayer;

import android.util.Log;

import javax.inject.Inject;

import androidx.lifecycle.Lifecycle;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import ru.nubby.playstream.domain.FollowsRepository;
import ru.nubby.playstream.domain.StreamsRepository;
import ru.nubby.playstream.domain.UsersRepository;
import ru.nubby.playstream.domain.entities.Quality;
import ru.nubby.playstream.domain.entities.QualityLinks;
import ru.nubby.playstream.domain.entities.Stream;
import ru.nubby.playstream.domain.interactors.PreferencesInteractor;
import ru.nubby.playstream.domain.interactors.StreamsInteractor;
import ru.nubby.playstream.presentation.base.BaseRxPresenter;
import ru.nubby.playstream.utils.RxSchedulersProvider;

import static ru.nubby.playstream.presentation.stream.streamplayer.StreamContract.View.InfoMessage.ERROR_CHANNEL_FOLLOW_UNFOLLOW;
import static ru.nubby.playstream.presentation.stream.streamplayer.StreamContract.View.InfoMessage.ERROR_FETCHING_ADDITIONAL_INFO;
import static ru.nubby.playstream.presentation.stream.streamplayer.StreamContract.View.InfoMessage.INFO_CHANNEL_FOLLOWED;
import static ru.nubby.playstream.presentation.stream.streamplayer.StreamContract.View.InfoMessage.INFO_CHANNEL_UNFOLLOWED;

public class StreamPresenter extends BaseRxPresenter<StreamContract.View>
        implements StreamContract.Presenter {

    private final String TAG = StreamPresenter.class.getSimpleName();

    private Disposable mStreamResolutionsInfoTask;
    private Disposable mStreamAdditionalInfoTask;
    private Disposable mStreamInfoUpdater;
    private Disposable mFollowUnfollowTask;
    private Disposable mFollowDisplayTask;
    private QualityLinks mQualityUrls;

    private Stream mCurrentStream;

    private final StreamsRepository mStreamsRepository;
    private final FollowsRepository mFollowsRepository;
    private final UsersRepository mUsersRepository;
    private final StreamsInteractor mStreamsInteractor;
    private final PreferencesInteractor mPreferencesInteractor;

    private final RxSchedulersProvider mRxSchedulersProvider;

    @Inject
    public StreamPresenter(StreamsRepository streamsRepository,
                           FollowsRepository followsRepository,
                           UsersRepository usersRepository,
                           StreamsInteractor streamsInteractor,
                           PreferencesInteractor preferencesInteractor,
                           RxSchedulersProvider rxSchedulersProvider) {
        mStreamsRepository = streamsRepository;
        mFollowsRepository = followsRepository;
        mUsersRepository = usersRepository;
        mPreferencesInteractor = preferencesInteractor;
        mStreamsInteractor = streamsInteractor;
        mRxSchedulersProvider = rxSchedulersProvider;
    }

    @Override
    public void subscribe(StreamContract.View view, Lifecycle lifecycle, Stream stream) {
        super.subscribe(view, lifecycle);

        Stream streamCopy = new Stream(stream);
        Single<Stream> initialStreamRequest = mUsersRepository
                .getUserFromStreamer(stream)
                .map(updatedLogin -> {
                    streamCopy.setStreamerLogin(updatedLogin.getLogin());
                    return streamCopy;
                });

        mStreamAdditionalInfoTask = initialStreamRequest
                .doOnSubscribe(streamReturned -> mView.displayLoading(true))
                .observeOn(mRxSchedulersProvider.getUiScheduler())
                .subscribe(
                        streamReturned -> {
                            mCurrentStream = streamReturned;
                            playStream(streamReturned);
                            mView.displayLoading(false);
                            mView.displayTitle(streamReturned.getTitle());
                            mView.displayViewerCount(streamReturned.getViewerCount());
                            mFollowDisplayTask = mFollowsRepository
                                    .isStreamFollowed(streamReturned)
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
            mFollowUnfollowTask = mFollowsRepository
                    .isStreamFollowed(mCurrentStream)
                    .flatMapCompletable(result -> {
                        if (result) {
                            return mFollowsRepository.unfollowStream(mCurrentStream);
                        } else {
                            return mFollowsRepository.followStream(mCurrentStream);
                        }
                    })
                    .andThen(mFollowsRepository.isStreamFollowed(mCurrentStream))
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

        mStreamResolutionsInfoTask = mStreamsInteractor
                .getStreamLinks(stream)
                .doOnSubscribe((x) -> mView.displayLoading(true))
                .doFinally(() -> mView.displayLoading(false))
                .observeOn(mRxSchedulersProvider.getUiScheduler())
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

        mStreamInfoUpdater = mStreamsRepository
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
