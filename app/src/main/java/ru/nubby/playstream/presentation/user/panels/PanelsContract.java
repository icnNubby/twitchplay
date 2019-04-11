package ru.nubby.playstream.presentation.user.panels;

import android.view.View;

import java.util.List;

import androidx.lifecycle.Lifecycle;
import ru.nubby.playstream.domain.entities.ChannelPanel;
import ru.nubby.playstream.domain.entities.UserData;
import ru.nubby.playstream.presentation.base.BasePresenter;
import ru.nubby.playstream.presentation.base.BaseView;

public interface PanelsContract {

    interface View extends BaseView {

        enum ErrorMessage {
            ERROR_BAD_CONNECTION
        }

        void displayStub();

        void displayPanels(List<ChannelPanel> panels);

        void displayInfoMessage(ErrorMessage message);
    }

    interface Presenter extends BasePresenter<PanelsContract.View> {

        void subscribe(PanelsContract.View view, Lifecycle lifecycle, UserData user);

    }
}
