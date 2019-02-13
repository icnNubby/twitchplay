package ru.nubby.playstream.ui.streamlist.streamlistfragment;

import java.util.List;

import ru.nubby.playstream.ui.BasePresenter;
import ru.nubby.playstream.ui.BaseView;
import ru.nubby.playstream.model.Stream;

public interface StreamListContract {

    interface View extends BaseView<Presenter> {
        void displayNewStreamList(List<Stream> streams);
        void addStreamList(List<Stream> streams);
    }

    interface Presenter extends BasePresenter {
        void addMoreStreams();
        void updateStreams();
        void showStream(Stream stream);
        void getFollowedStreams();
        void getTopStreams();

    }

}
