package ru.nubby.playstream.data.database;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import ru.nubby.playstream.model.FollowRelations;
import ru.nubby.playstream.model.UserData;

public class RoomLocalDataSource implements LocalDataSource {

    private final FollowRelationsDao mFollowRelationsDao;
    private final UserDataDao mUserDataDao;

    public RoomLocalDataSource(FollowRelationsDao followRelationsDao, UserDataDao userDataDao) {
        mFollowRelationsDao = followRelationsDao;
        mUserDataDao = userDataDao;
    }

    @Override
    public Single<List<FollowRelations>> getFollowRelationsEntriesById(String fromId) {
        return mFollowRelationsDao.loadRelationsFromId(fromId);
    }

    @Override
    public Single<List<FollowRelations>> findRelation(String fromId, String toId) {
        return mFollowRelationsDao.findRelation(fromId, toId);
    }


    @Override
    public Completable insertFollowRelationsEntry(FollowRelations followRelationsEntry) {
        return mFollowRelationsDao.insertFollowRelationsEntry(followRelationsEntry);
    }

    @Override
    public Completable insertFollowRelationsList(FollowRelations... followRelationsList) {

        return mFollowRelationsDao.insertListFollowRelationsEntries(followRelationsList);
    }

    @Override
    public Completable deleteFollowRelationsEntry(FollowRelations followRelationsEntry) {
        return mFollowRelationsDao.deleteFollowRelationsEntry(followRelationsEntry);
    }

    @Override
    public Completable deleteAllFollowRelationsEntries() {
        return mFollowRelationsDao.deleteAllFollowRelationsEntries();
    }

    @Override
    public Maybe<UserData> findUserDataById(String id) {
        return mUserDataDao.findUserDataById(id);
    }

    @Override
    public Completable insertUserData(UserData userDataEntry) {
        return mUserDataDao.insertUserData(userDataEntry);
    }

    @Override
    public Completable deleteUserDataEntry(UserData userDataEntry) {
        return mUserDataDao.deleteUserDataEntry(userDataEntry);
    }

    @Override
    public Completable deleteUserDataEntries() {
        return mUserDataDao.deleteUserDataEntries();
    }
}
