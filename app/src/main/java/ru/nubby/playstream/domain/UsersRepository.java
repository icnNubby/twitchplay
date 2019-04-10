package ru.nubby.playstream.domain;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;
import ru.nubby.playstream.domain.entities.ChannelInfoV5;
import ru.nubby.playstream.domain.entities.ChannelPanel;
import ru.nubby.playstream.domain.entities.Stream;
import ru.nubby.playstream.domain.entities.UserData;

public interface UsersRepository {

    /**
     * Gets {@link UserData} bound to that stream for further queries.
     *
     * @param stream stream object
     * @return user data object (watch link).
     */
    Single<UserData> getUserFromStreamer(Stream stream);

    /**
     * Gets {@link UserData} for currently logged user.
     *
     * @param token String OAUTH2 token
     * @return user data object, related to logged user.
     */
    Single<UserData> getUserFromToken(String token);

    /**
     * Updates all userData entries in local database from remote.
     * @return Might return error in rx style if something happened.
     */
    Completable synchronizeUserData();

    /**
     * Gets all UserData for given id's.
     * @param usersIds list of id's to search.
     * @return list of {@link UserData} objects.
     */
    Single<List<UserData>> getUsersByIds(List<String> usersIds);

    /**
     * Gets panels objects for given user id.
     * @param userId user id to request.
     * @return list of {@link ChannelPanel} objects.
     */
    Single<List<ChannelPanel>> getPanelsForUser(String userId);

    /**
     * Gets old {@link ChannelInfoV5} object for given channel( == user) id.
     * @param userId user (same as channel) id.
     * @return one {@link ChannelInfoV5} object.
     */
    Single<ChannelInfoV5> getOldChannelInfo(String userId);
}
