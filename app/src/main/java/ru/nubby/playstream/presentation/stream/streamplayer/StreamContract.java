package ru.nubby.playstream.presentation.stream.streamplayer;

import java.util.List;

import androidx.lifecycle.Lifecycle;
import ru.nubby.playstream.domain.entities.Stream;
import ru.nubby.playstream.presentation.base.BasePresenter;
import ru.nubby.playstream.presentation.base.BaseView;
import ru.nubby.playstream.domain.entities.Quality;

public interface StreamContract {
    interface View extends BaseView {
        enum InfoMessage {
            INFO_CHANNEL_FOLLOWED,
            INFO_CHANNEL_UNFOLLOWED,
            ERROR_CHANNEL_FOLLOW_UNFOLLOW,
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

    interface Presenter extends BasePresenter<View> {
        void subscribe(View view, Lifecycle lifecycle, Stream stream);
        void playChosenQuality(Quality quality);
        void followOrUnfollowChannel();
    }
}
