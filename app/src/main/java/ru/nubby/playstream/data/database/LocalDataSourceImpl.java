package ru.nubby.playstream.data.database;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import ru.nubby.playstream.model.FollowRelations;

public class LocalDataSourceImpl implements  LocalDataSource {

    private final FollowRelationsDao mFollowRelationsDao;

    public LocalDataSourceImpl(FollowRelationsDao appDao) {
        mFollowRelationsDao = appDao;
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
    public Completable deleteAllEntries() {
        return mFollowRelationsDao.deleteAllFollowRelationsEntries();
    }
}
