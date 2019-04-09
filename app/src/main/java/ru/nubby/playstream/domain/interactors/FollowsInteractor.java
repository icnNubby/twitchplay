package ru.nubby.playstream.domain.interactors;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Completable;
import io.reactivex.Single;
import ru.nubby.playstream.domain.FollowsRepository;
import ru.nubby.playstream.domain.entities.Stream;

@Singleton
public class FollowsInteractor {

    private final FollowsRepository mFollowsRepository;

    @Inject
    public FollowsInteractor(FollowsRepository followsRepository) {

        mFollowsRepository = followsRepository;
    }

    public Completable followStream(Stream targetStream) {
        return mFollowsRepository.followStream(targetStream);
    }

    public Completable unfollowStream(Stream targetStream) {
        return mFollowsRepository.unfollowStream(targetStream);
    }

    public Single<Boolean> isStreamFollowed(Stream targetStream) {
        return mFollowsRepository.isStreamFollowed(targetStream);
    }

    public Completable synchronizeFollows(String userId) {
        return mFollowsRepository.synchronizeFollows(userId);
    }

}
