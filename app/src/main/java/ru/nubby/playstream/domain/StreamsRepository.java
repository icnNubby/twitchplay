package ru.nubby.playstream.domain;

import java.util.HashMap;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import ru.nubby.playstream.domain.entities.FollowRelations;
import ru.nubby.playstream.domain.entities.Pagination;
import ru.nubby.playstream.domain.entities.Quality;
import ru.nubby.playstream.domain.entities.Stream;
import ru.nubby.playstream.domain.entities.StreamsResponse;
import ru.nubby.playstream.domain.entities.UserData;

public interface StreamsRepository {

    /**
     * Gets stream list from remote repository
     * @return list of top streams
     */
    Single<StreamsResponse> getTopStreams();

    /**
     * Gets stream list from remote repository
     * @param pagination pagination cursor
     * @return list of streams after pagination cursor
     */
    Single<StreamsResponse> getTopStreams(Pagination pagination);

    /**
     * Gets active stream list from remote or local(cached) repository
     * @return list of user's followed streams
     */
    Single<List<Stream>> getLiveStreamsFollowedByUser();

    /**
     * Gets video url from stream object
     *
     * @param stream stream object
     * @return HashMap of Qualities as keys, and Urls to hls resources as values
     */
    Single<HashMap<Quality, String>> getQualityUrls(Stream stream);

    /**
     * Gets new {@link Stream} information, such as user counter, state, etc.
     * Does not update initial object, returns new and updated one.
     *
     * @param stream Stream object
     * @return new Stream object.
     */
    Observable<Stream> getUpdatableStreamInfo(Stream stream);

}
