package ru.nubby.playstream.ui.streamlist.streamlistfragment;

import java.util.List;

import ru.nubby.playstream.ui.BasePresenter;
import ru.nubby.playstream.ui.BaseView;
import ru.nubby.playstream.model.Stream;

public interface StreamListContract {

    interface View extends BaseView<Presenter> {
        void displayNewStreamList(List<Stream> streams);
        void clearStreamList();
        void addStreamList(List<Stream> streams);
        void setupProgressBar(boolean visible);
    }

    interface Presenter extends BasePresenter {
        void getMoreTopStreams();
        void updateStreams();
        void getFollowedStreams();
        void getTopStreams();

    }

}
