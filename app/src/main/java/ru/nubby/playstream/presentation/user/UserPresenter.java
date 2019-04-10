package ru.nubby.playstream.presentation.user;

import androidx.lifecycle.Lifecycle;
import io.reactivex.disposables.Disposable;
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

    private final UsersInteractor mUsersInteractor;
    private final FollowsInteractor mFollowsInteractor;
    private final RxSchedulersProvider mRxSchedulersProvider;

    private UserData mUserData;


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
        mUserData = user;
        mView.displayUser(user);
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
}
