package ru.nubby.playstream.data.database;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;
import ru.nubby.playstream.model.FollowRelations;

public class RoomLocalDataSource implements  LocalDataSource {

    private final FollowRelationsDao mFollowRelationsDao;

    public RoomLocalDataSource(FollowRelationsDao followRelationsDao) {
        mFollowRelationsDao = followRelationsDao;
    }

    @Override
    public Single<List<FollowRelations>> getFollowRelationsEntriesById(String fromId) {
        return mFollowRelationsDao.loadRelationsFromId(fromId);
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
}
