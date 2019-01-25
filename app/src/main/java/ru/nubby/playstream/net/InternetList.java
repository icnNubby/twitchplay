package ru.nubby.playstream.net;

import java.util.List;

import io.reactivex.Flowable;
import ru.nubby.playstream.model.Stream;

public class InternetList implements Repository {
    @Override
    public Flowable<List<Stream>> getStreams(ResponceListener callback) {
        RetrofitSingleton.getInstance().getStreamsTest(callback);
        return null;
    }
}
