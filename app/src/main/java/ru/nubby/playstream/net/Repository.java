package ru.nubby.playstream.net;

import java.util.List;

import io.reactivex.Observable;
import ru.nubby.playstream.model.Stream;

public interface Repository {
    Observable<List<Stream>> getStreams();
}
