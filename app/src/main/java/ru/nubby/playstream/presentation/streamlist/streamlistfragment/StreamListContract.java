package ru.nubby.playstream.presentation.streamlist.streamlistfragment;

import java.util.List;

import ru.nubby.playstream.presentation.BasePresenter;
import ru.nubby.playstream.presentation.BaseView;
import ru.nubby.playstream.model.Stream;

public interface StreamListContract {

    interface View extends BaseView<Presenter> {
        enum ErrorMessage {
            ERROR_BAD_CONNECTION
        }
        void displayStreamList(List<Stream> streams);
        void clearStreamList();
        void addStreamList(List<Stream> streams);
        void setupProgressBar(boolean visible);
        void displayError(ErrorMessage message);
        void setPreviewSize(int size);
    }

    interface Presenter extends BasePresenter {
        void getMoreStreams();
        void updateStreams();
        void getFollowedStreams();
        void getTopStreams();
        void decideToReload(long interval);
    }

}
