package ru.nubby.playstream.twitchapi;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import ru.nubby.playstream.model.FollowRelations;
import ru.nubby.playstream.model.Pagination;
import ru.nubby.playstream.model.Stream;
import ru.nubby.playstream.model.StreamsRequest;

public interface Repository {
    /**
     * Gets stream list from remote or local(cached) repository
     * @return list of top streams
     */
    Single<StreamsRequest> getStreams();

    /**
     * Gets stream list from remote or local(cached) repository
     * @param pagination {@link Pagination} cursor
     * @return list of streams after pagination cursor
     */
    Single<StreamsRequest> getStreams(Pagination pagination);

    /**
     * Gets user follows list from remote or local(cached) repository
     * @param userId current (logged) user id
     * @return list of user's followed streams
     */
    Single<List<FollowRelations>> getUserFollows(String userId);
}
