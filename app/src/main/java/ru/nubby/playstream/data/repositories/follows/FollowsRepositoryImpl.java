package ru.nubby.playstream.data.repositories.follows;


import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import androidx.annotation.NonNull;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Single;
import ru.nubby.playstream.data.sources.database.LocalRepository;
import ru.nubby.playstream.data.sources.twitchapi.RemoteRepository;
import ru.nubby.playstream.domain.FollowsRepository;
import ru.nubby.playstream.domain.entities.FollowRelations;
import ru.nubby.playstream.domain.entities.Stream;
import ru.nubby.playstream.domain.entities.UserData;
import ru.nubby.playstream.domain.interactors.AuthInteractor;
import ru.nubby.playstream.utils.RxSchedulersProvider;

@Singleton
public class FollowsRepositoryImpl implements FollowsRepository {

    private final RemoteRepository mRemoteRepository;
    private final LocalRepository mLocalRepository;
    private final AuthInteractor mAuthInteractor;
    private final RxSchedulersProvider mRxSchedulersProvider;

    private boolean mFollowsFullUpdate = true;

    @Inject
    public FollowsRepositoryImpl(@NonNull RemoteRepository remoteRepository,
                                 @NonNull LocalRepository localRepository,
                                 @NonNull AuthInteractor authInteractor,
                                 @NonNull RxSchedulersProvider rxSchedulersProvider) {

        mRemoteRepository = remoteRepository;
        mLocalRepository = localRepository;
        mAuthInteractor = authInteractor;
        mRxSchedulersProvider = rxSchedulersProvider;
    }

    @Override
    public Completable followUser(UserData targetUser) {
        return mAuthInteractor
                .getCurrentLoginInfo()
                .subscribeOn(mRxSchedulersProvider.getIoScheduler())
                .flatMapCompletable(
                        userData ->
                                mRemoteRepository
                                        .followTargetUser(
                                                mAuthInteractor.getOauthToken(),
                                                userData.getId(),
                                                targetUser.getId())
                                        .andThen(mLocalRepository
                                                .insertFollowRelationsEntry(
                                                        new FollowRelations(userData.getId(),
                                                                userData.getLogin(),
                                                                targetUser.getId(),
                                                                targetUser.getLogin(),
                                                                ""))));
        //todo fix empty fields;
    }

    @Override
    public Completable unfollowUser(UserData targetUser) {
        return mAuthInteractor
                .getCurrentLoginInfo()
                .subscribeOn(mRxSchedulersProvider.getIoScheduler())
                .flatMapCompletable(userData ->
                        mRemoteRepository
                                .unfollowTargetUser(mAuthInteractor.getOauthToken(),
                                        userData.getId(), targetUser.getId())
                                .andThen(mLocalRepository
                                        .deleteFollowRelationsEntry(
                                                new FollowRelations(userData.getId(),
                                                        userData.getLogin(),
                                                        targetUser.getId(),
                                                        targetUser.getLogin(),
                                                        ""))));
    }

    @Override
    public Single<Boolean> isUserFollowed(UserData targetUser) {
        return mAuthInteractor
                .getCurrentLoginInfo()
                .subscribeOn(mRxSchedulersProvider.getIoScheduler())
                .flatMap(userData -> mLocalRepository.findRelation(userData.getId(),
                        targetUser.getId()))
                .flatMap(followRelationsList ->
                        Single.create(emitter -> emitter.onSuccess(!followRelationsList.isEmpty())));
    }

    @Override
    public Completable synchronizeFollows(String userId) {
        mFollowsFullUpdate = true;
        return getUserFollows(userId)
                .flatMapCompletable(followRelationsList -> CompletableObserver::onComplete);
    }

    @Override
    public Single<List<FollowRelations>> getUserFollows(String userId) {
        if (mFollowsFullUpdate) {
            return mRemoteRepository
                    .getUserFollows(userId)
                    .doOnSuccess(followRelationsList -> mFollowsFullUpdate = false)
                    .flatMap(followRelationsList ->
                            mLocalRepository
                                    .deleteAllFollowRelationsEntries()
                                    .andThen(mLocalRepository
                                            .insertFollowRelationsList(followRelationsList.toArray(
                                                    new FollowRelations[0])))
                                    .andThen(Single.just(followRelationsList)));

        } else {
            return mLocalRepository
                    .getFollowRelationsEntriesById(userId);
        }
    }

}
