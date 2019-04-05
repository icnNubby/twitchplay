package ru.nubby.playstream.data.sources.database;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import ru.nubby.playstream.domain.entities.FollowRelations;
import ru.nubby.playstream.domain.entities.UserData;

public interface LocalRepository {

    Single<List<FollowRelations>> getFollowRelationsEntriesById(String fromId);

    Single<List<FollowRelations>> findRelation(String fromId, String toId);

    Completable insertFollowRelationsEntry(FollowRelations followRelationsEntry);

    Completable insertFollowRelationsList(FollowRelations... followRelationsList);

    Completable deleteFollowRelationsEntry(FollowRelations followRelationsEntry);

    Completable deleteAllFollowRelationsEntries();

    Maybe<UserData> findUserDataById(String id);

    Maybe<List<UserData>> getAllUserDataEntries();

    Completable insertUserData(UserData userDataEntry);

    Completable insertUserDataList(UserData... userDataEntryList);

    Completable deleteUserDataEntry(UserData userDataEntry);

    Completable deleteUserDataEntries();

}
