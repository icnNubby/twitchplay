package ru.nubby.playstream.ui.streamlist.streamlistfragment;

import java.util.List;
import java.util.Map;

import ru.nubby.playstream.model.UserData;
import ru.nubby.playstream.ui.BasePresenter;
import ru.nubby.playstream.ui.BaseView;
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
    }

    interface Presenter extends BasePresenter {
        void getMoreStreams();
        void updateStreams();
        void getFollowedStreams();
        void getTopStreams();
        void decideToReload(long interval);
    }

}
