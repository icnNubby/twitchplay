package ru.nubby.playstream.data.sources.database;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import ru.nubby.playstream.domain.entities.FollowRelations;
import ru.nubby.playstream.domain.entities.Game;
import ru.nubby.playstream.domain.entities.UserData;
import ru.nubby.playstream.utils.RxSchedulersProvider;

@Singleton
public class RoomLocalDataSource implements LocalRepository {

    private final FollowRelationsDao mFollowRelationsDao;
    private final UserDataDao mUserDataDao;
    private final GamesDao mGamesDao;

    private final Scheduler mIoScheduler;
    private final Scheduler mComputationScheduler;


    @Inject
    public RoomLocalDataSource(FollowRelationsDao followRelationsDao,
                               UserDataDao userDataDao,
                               GamesDao gamesDao,
                               RxSchedulersProvider schedulersProvider) {
        mFollowRelationsDao = followRelationsDao;
        mUserDataDao = userDataDao;
        mGamesDao = gamesDao;
        mIoScheduler = schedulersProvider.getIoScheduler();
        mComputationScheduler = schedulersProvider.getComputationScheduler();
    }

    @Override
    public Single<List<FollowRelations>> getFollowRelationsEntriesById(String fromId) {
        return mFollowRelationsDao
                .loadRelationsFromId(fromId)
                .subscribeOn(mIoScheduler);
    }

    @Override
    public Single<List<FollowRelations>> findRelation(String fromId, String toId) {
        return mFollowRelationsDao
                .findRelation(fromId, toId)
                .subscribeOn(mIoScheduler);
    }


    @Override
    public Completable insertFollowRelationsEntry(FollowRelations followRelationsEntry) {
        return mFollowRelationsDao
                .insertFollowRelationsEntry(followRelationsEntry)
                .subscribeOn(mIoScheduler);
    }

    @Override
    public Completable insertFollowRelationsList(FollowRelations... followRelationsList) {

        return mFollowRelationsDao
                .insertListFollowRelationsEntries(followRelationsList)
                .subscribeOn(mIoScheduler);
    }

    @Override
    public Completable deleteFollowRelationsEntry(FollowRelations followRelationsEntry) {
        return mFollowRelationsDao
                .deleteFollowRelationsEntry(followRelationsEntry)
                .subscribeOn(mIoScheduler);
    }

    @Override
    public Completable deleteAllFollowRelationsEntries() {
        return mFollowRelationsDao
                .deleteAllFollowRelationsEntries()
                .subscribeOn(mIoScheduler);
    }

    @Override
    public Maybe<UserData> findUserDataById(String id) {
        return mUserDataDao
                .findUserDataById(id)
                .subscribeOn(mIoScheduler);
    }

    @Override
    public Maybe<List<UserData>> findUserDataByIdList(List<String> idList) {
        return mUserDataDao
                .findUserDataByIdList(idList)
                .subscribeOn(mIoScheduler);
    }


    @Override
    public Maybe<List<UserData>> getAllUserDataEntries() {
        return mUserDataDao
                .getAll()
                .subscribeOn(mIoScheduler);
    }

    @Override
    public Completable insertUserData(UserData userDataEntry) {
        return mUserDataDao
                .insertUserData(userDataEntry)
                .subscribeOn(mIoScheduler);
    }

    @Override
    public Completable insertUserDataList(UserData... userDataEntryList) {
        return mUserDataDao
                .insertUserDataList(userDataEntryList)
                .subscribeOn(mIoScheduler);
    }

    @Override
    public Completable deleteUserDataEntry(UserData userDataEntry) {
        return mUserDataDao
                .deleteUserDataEntry(userDataEntry)
                .subscribeOn(mIoScheduler);
    }

    @Override
    public Completable deleteUserDataEntries() {
        return mUserDataDao
                .deleteUserDataEntries()
                .subscribeOn(mIoScheduler);
    }

    @Override
    public  Maybe<Game> findGame(String id) {
        return mGamesDao
                .findGame(id)
                .subscribeOn(mIoScheduler);
    }

    @Override
    public Maybe<List<Game>> findGames(List<String> ids) {
        return mGamesDao
                .findGames(ids)
                .subscribeOn(mIoScheduler);
    }

    @Override
    public Completable insertGame(Game game) {
        return mGamesDao
                .insertGame(game)
                .subscribeOn(mIoScheduler);
    }

    @Override
    public Completable insertGameList(Game... games) {
        return mGamesDao
                .insertGameList(games)
                .subscribeOn(mIoScheduler);
    }

    @Override
    public Completable deleteGame(Game game) {
        return mGamesDao
                .deleteGame(game)
                .subscribeOn(mIoScheduler);
    }

    @Override
    public Completable deleteAllGames() {
        return mGamesDao
                .deleteAllGames()
                .subscribeOn(mIoScheduler);
    }
}
