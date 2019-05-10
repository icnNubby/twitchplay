package ru.nubby.playstream.domain.interactors;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Completable;
import io.reactivex.Single;
import ru.nubby.playstream.domain.FollowsRepository;
import ru.nubby.playstream.domain.entities.Stream;
import ru.nubby.playstream.domain.entities.UserData;

/**
 * Follows bisiness logic.
 */
@Singleton
public class FollowsInteractor {

    private final FollowsRepository mFollowsRepository;

    @Inject
    public FollowsInteractor(FollowsRepository followsRepository) {

        mFollowsRepository = followsRepository;
    }

    public Completable followUser(UserData targetUser) {
        return mFollowsRepository.followUser(targetUser);
    }

    public Completable unfollowUser(UserData targetUser) {
        return mFollowsRepository.unfollowUser(targetUser);
    }

    public Single<Boolean> isUserFollowed(UserData targetUser) {
        return mFollowsRepository.isUserFollowed(targetUser);
    }

    public Completable synchronizeFollows(String userId) {
        return mFollowsRepository.synchronizeFollows(userId);
    }

}
