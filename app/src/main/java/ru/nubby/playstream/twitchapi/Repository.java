package ru.nubby.playstream.twitchapi;

import java.util.List;

import io.reactivex.Observable;
import ru.nubby.playstream.model.Stream;

public interface Repository {
    /**
     * Gets stream list from remote or local(cached) repository
     * @return list of top streams
     */
    Observable<List<Stream>> getStreams();
}
