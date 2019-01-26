package ru.nubby.playstream.net;

import java.util.List;

import io.reactivex.Observable;
import ru.nubby.playstream.model.Stream;

public class InternetList implements Repository {
    @Override
    public Observable<List<Stream>> getStreams() {
        return RetrofitSingleton.getInstance().getStreamsTest();
    }
}
