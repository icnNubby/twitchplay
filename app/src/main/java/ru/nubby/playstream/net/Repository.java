package ru.nubby.playstream.net;

import java.util.List;

import io.reactivex.Observable;
import ru.nubby.playstream.model.Stream;

public interface Repository {
    /**
     * Gets stream list from remote or local(cached) repository
     * @returns Observable(List(Stream))
     */
    Observable<List<Stream>> getStreams();
}
