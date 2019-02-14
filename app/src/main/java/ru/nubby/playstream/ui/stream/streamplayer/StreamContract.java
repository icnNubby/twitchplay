package ru.nubby.playstream.ui.stream.streamplayer;

import java.util.List;

import ru.nubby.playstream.ui.BasePresenter;
import ru.nubby.playstream.ui.BaseView;
import ru.nubby.playstream.model.Stream;
import ru.nubby.playstream.utils.Quality;

public interface StreamContract {
    interface View extends BaseView<Presenter> {
        void displayStream(String url);
        void setQualitiesMenu(List<Quality> qualities);
        void displayLoading(boolean loadingState);
        void displayTitle(String title);
        void displayViewerCount(String count);
    }

    interface Presenter extends BasePresenter {
        void playChosenQuality(Quality quality);
        void pauseStream();
        void playStream(Stream stream);
        void volumeTune();
    }
}
