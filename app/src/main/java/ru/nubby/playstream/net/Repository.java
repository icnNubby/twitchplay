package ru.nubby.playstream.net;

import java.util.List;

import io.reactivex.Flowable;
import ru.nubby.playstream.model.Stream;

public interface Repository {
    Flowable<List<Stream>> getStreams(ResponceListener callback);
}
