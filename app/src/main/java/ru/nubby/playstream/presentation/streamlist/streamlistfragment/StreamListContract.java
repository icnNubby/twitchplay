package ru.nubby.playstream.presentation.streamlist.streamlistfragment;

import java.util.List;

import androidx.lifecycle.Lifecycle;
import ru.nubby.playstream.presentation.base.BasePresenter;
import ru.nubby.playstream.presentation.base.BaseView;
import ru.nubby.playstream.domain.entities.Stream;

public interface StreamListContract {

    interface View extends BaseView {
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

    interface Presenter extends BasePresenter<View> {
        void subscribe(StreamListContract.View view, Lifecycle lifecycle, long interval);
        void getMoreStreams();
        void updateStreams();
        void getFollowedStreams();
        void getTopStreams();
    }

}
