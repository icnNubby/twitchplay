package ru.nubby.playstream.presentation.user;

import android.util.Log;

import androidx.lifecycle.Lifecycle;
import io.reactivex.disposables.Disposable;
import ru.nubby.playstream.domain.entities.ChannelInfoV5;
import ru.nubby.playstream.domain.entities.UserData;
import ru.nubby.playstream.domain.interactors.FollowsInteractor;
import ru.nubby.playstream.domain.interactors.UsersInteractor;
import ru.nubby.playstream.presentation.base.BaseRxPresenter;
import ru.nubby.playstream.utils.RxSchedulersProvider;

import static ru.nubby.playstream.presentation.stream.streamplayer.StreamContract.View.InfoMessage.ERROR_CHANNEL_FOLLOW_UNFOLLOW;
import static ru.nubby.playstream.presentation.stream.streamplayer.StreamContract.View.InfoMessage.INFO_CHANNEL_FOLLOWED;
import static ru.nubby.playstream.presentation.stream.streamplayer.StreamContract.View.InfoMessage.INFO_CHANNEL_UNFOLLOWED;

public class UserPresenter extends BaseRxPresenter<UserContract.View>
        implements UserContract.Presenter {

    private static final String TAG = UserPresenter.class.getSimpleName();

    private final UsersInteractor mUsersInteractor;
    private final FollowsInteractor mFollowsInteractor;
    private final RxSchedulersProvider mRxSchedulersProvider;

    private UserData mUserData;
    private ChannelInfoV5 mChannelInfoV5;


    public UserPresenter(FollowsInteractor followsInteractor,
                         UsersInteractor usersInteractor,
                         RxSchedulersProvider rxSchedulersProvider) {
        mFollowsInteractor = followsInteractor;
        mUsersInteractor = usersInteractor;
        mRxSchedulersProvider = rxSchedulersProvider;
    }

    @Override
    public void subscribe(UserContract.View view, Lifecycle lifecycle, UserData user) {
        super.subscribe(view, lifecycle);
        if (user != null) {
            mUserData = user;
        }
        mView.displayUser(mUserData);
        if (mChannelInfoV5 == null) {
            Disposable getV5User = mUsersInteractor
                    .getOldInfoForUser(mUserData.getId())
                    .observeOn(mRxSchedulersProvider.getUiScheduler())
                    .subscribe(
                            channelInfo -> {
                                mChannelInfoV5 = channelInfo;
                                displayFetchedInfoV5();
                            },
                            error -> {
                                Log.e(TAG, "Error while loading old user.", error);
                            });
            mCompositeDisposable.add(getV5User);
        } else {
            displayFetchedInfoV5();
        }
    }


    @Override
    public void unsubscribe() {
        //todo
    }


    @Override
    public void followOrUnfollowChannel() {
        Disposable followUnfollowTask = mFollowsInteractor
                .isUserFollowed(mUserData)
                .flatMapCompletable(result -> {
                    if (result) {
                        return mFollowsInteractor.unfollowUser(mUserData);
                    } else {
                        return mFollowsInteractor.followUser(mUserData);
                    }
                })
                .andThen(mFollowsInteractor.isUserFollowed(mUserData))
                .observeOn(mRxSchedulersProvider.getUiScheduler())
                .subscribe(
                        followStatus -> {
                            mView.displayFollowStatus(followStatus);
                            mView.enableFollow(true);
                            if (followStatus) {
                                mView.displayInfoMessage(INFO_CHANNEL_FOLLOWED,
                                        mUserData.getDisplayName());
                            } else {
                                mView.displayInfoMessage(INFO_CHANNEL_UNFOLLOWED,
                                        mUserData.getDisplayName());
                            }
                        },
                        error -> {
                            mView.enableFollow(false);
                            mView.displayInfoMessage(ERROR_CHANNEL_FOLLOW_UNFOLLOW,
                                    mUserData.getDisplayName());
                        });
        mCompositeDisposable.add(followUnfollowTask);
    }

    private void displayFetchedInfoV5() {
        String url = extractBannerUrl(mChannelInfoV5);
        if (!url.isEmpty()) {
            mView.setupBackground(url);
        }
        mView.displayFollowersCount(mChannelInfoV5.getFollowers());
    }

    private String extractBannerUrl(ChannelInfoV5 channelInfo) {
        String url = "";
        if (channelInfo.getProfileBanner() != null &&
                !channelInfo.getProfileBanner().isEmpty()) {
            url = channelInfo.getProfileBanner();
        } else if (mUserData.getOfflineImageUrl() != null &&
                !mUserData.getOfflineImageUrl().isEmpty()) {
            url = mUserData.getOfflineImageUrl();
        }
        return url;
    }
}
