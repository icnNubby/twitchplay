package ru.nubby.playstream.data.database;

import java.util.List;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import ru.nubby.playstream.model.FollowRelations;
import ru.nubby.playstream.model.UserData;

public interface LocalDataSource {

    Single<List<FollowRelations>> getFollowRelationsEntriesById(String fromId);

    Single<List<FollowRelations>> findRelation(String fromId, String toId);

    Completable insertFollowRelationsEntry(FollowRelations followRelationsEntry);

    Completable insertFollowRelationsList(FollowRelations... followRelationsList);

    Completable deleteFollowRelationsEntry(FollowRelations followRelationsEntry);

    Completable deleteAllFollowRelationsEntries();

    Maybe<UserData> findUserDataById(String id);

    Completable insertUserData(UserData userDataEntry);

    Completable deleteUserDataEntry(UserData userDataEntry);

    Completable deleteUserDataEntries();

}
