package ru.nubby.playstream.ui.streamlist;

public class StreamListActivityPresenter implements StreamListActivityContract.Presenter {
    StreamListActivityContract.View mMainStreamListView;

    public StreamListActivityPresenter(StreamListActivityContract.View view) {
        mMainStreamListView = view;
        mMainStreamListView.setPresenter(this);
    }

    @Override
    public void login(String token) {
        //TODO handle logging (write to Shared etc.
        //TODO  logic to handle current login if is some
    }

    @Override
    public void subscribe() {

    }

    @Override
    public void unsubscribe() {

    }
}
