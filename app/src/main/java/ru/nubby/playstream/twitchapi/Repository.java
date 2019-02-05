package ru.nubby.playstream.twitchapi;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import ru.nubby.playstream.model.Stream;

public interface Repository {
    /**
     * Gets stream list from remote or local(cached) repository
     * @return list of top streams
     */
    Single<List<Stream>> getStreams();
}
