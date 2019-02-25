package ru.nubby.playstream.ui.stream.streamplayer;

import java.util.List;

import ru.nubby.playstream.ui.BasePresenter;
import ru.nubby.playstream.ui.BaseView;
import ru.nubby.playstream.model.Quality;

public interface StreamContract {
    interface View extends BaseView<Presenter> {
        enum InfoMessage {
            INFO_CHANNEL_FOLLOWED, INFO_CHANNEL_UNFOLLOWED, ERROR_CHANNEL_FOLLOW_UNFOLLOW,
            ERROR_FETCHING_ADDITIONAL_INFO
        }
        void displayStream(String url);
        void displayInfoMessage(InfoMessage message, String streamerName);
        void setQualitiesMenu(List<Quality> qualities);
        void displayLoading(boolean loadingState);
        void displayTitle(String title);
        void displayViewerCount(String count);
        void displayFollowStatus(boolean followed);
        void toggleFullscreen(boolean currentModeFullscreenOn);
        void enableFollow(boolean enabled);
    }

    interface Presenter extends BasePresenter {
        void playChosenQuality(Quality quality);
        void followOrUnfollowChannel();
    }
}
