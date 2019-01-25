package ru.nubby.playstream.streamlist;

import java.util.List;

import ru.nubby.playstream.BasePresenter;
import ru.nubby.playstream.BaseView;
import ru.nubby.playstream.model.Stream;

public interface StreamListContract {

    interface View extends BaseView<Presenter> {
        void displayStreamList(List<Stream> streams);
    }

    interface Presenter extends BasePresenter {
        void addMoreStreams();
        void updateStreams();
    }

}
