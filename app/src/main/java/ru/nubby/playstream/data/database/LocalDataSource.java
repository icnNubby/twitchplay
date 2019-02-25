package ru.nubby.playstream.data.database;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;
import ru.nubby.playstream.model.FollowRelations;

public interface LocalDataSource {

    Single<List<FollowRelations>> getFollowRelationsEntriesById(String fromId);

    Single<List<FollowRelations>> findRelation(String fromId, String toId);

    Completable insertFollowRelationsEntry(FollowRelations followRelationsEntry);

    Completable insertFollowRelationsList(FollowRelations... followRelationsList);

    Completable deleteFollowRelationsEntry(FollowRelations followRelationsEntry);

    Completable deleteAllFollowRelationsEntries();

}