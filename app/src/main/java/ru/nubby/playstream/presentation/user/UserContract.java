package ru.nubby.playstream.presentation.user;

import androidx.lifecycle.Lifecycle;
import ru.nubby.playstream.domain.entities.UserData;
import ru.nubby.playstream.presentation.base.BasePresenter;
import ru.nubby.playstream.presentation.base.BaseView;

public interface UserContract {

    interface View extends BaseView {

        enum InfoMessage {
            INFO_CHANNEL_FOLLOWED,
            INFO_CHANNEL_UNFOLLOWED,
            ERROR_CHANNEL_FOLLOW_UNFOLLOW
        }

        void displayUser(UserData user);

        void setupBackground(String url);

        void displayFollowersCount(int followers);

        void displayFollowStatus(boolean followed);

        void displayInfoMessage(InfoMessage message, String streamerName);

        void enableFollow(boolean enabled);
    }

    interface Presenter extends BasePresenter<View> {
        void subscribe(View view, Lifecycle lifecycle, UserData user);

        void followOrUnfollowChannel();
    }

}
