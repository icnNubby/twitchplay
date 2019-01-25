package ru.nubby.playstream.streamlist;

import java.util.List;

import ru.nubby.playstream.model.Stream;
import ru.nubby.playstream.net.InternetList;
import ru.nubby.playstream.net.Repository;
import ru.nubby.playstream.net.ResponceListener;

public class StreamListPresenter implements StreamListContract.Presenter, ResponceListener {
    private StreamListContract.View mStreamListView;

    public StreamListPresenter(StreamListContract.View streamListView) {
        this.mStreamListView = streamListView;
        mStreamListView.setPresenter(this);
    }

    @Override
    public void addMoreStreams() {
        Repository internet = new InternetList(); //TODO INJECT
        internet.getStreams(this);
    }

    @Override
    public void updateStreams() {
            //TODO
    }

    @Override
    public void subscribe() {
            //TODO
    }

    @Override
    public void unsubscribe() {
        //TODO
    }

    @Override
    public void callback(List<Stream> list) {
        mStreamListView.displayStreamList(list);
    }
}
