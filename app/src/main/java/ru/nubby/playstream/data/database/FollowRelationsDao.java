package ru.nubby.playstream.data.database;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import io.reactivex.Completable;
import io.reactivex.Single;
import ru.nubby.playstream.domain.entity.FollowRelations;

@Dao
public interface FollowRelationsDao {

        @Query("SELECT * FROM follow_relations WHERE from_id = :fromId")
        Single<List<FollowRelations>> loadRelationsFromId(String fromId);

        @Query("SELECT * FROM follow_relations WHERE from_id = :fromId AND to_id = :toId")
        Single<List<FollowRelations>> findRelation(String fromId, String toId);

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        Completable insertFollowRelationsEntry(FollowRelations followRelationsEntry);

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        Completable insertListFollowRelationsEntries(FollowRelations... followRelationsEntry);

        @Delete
        Completable deleteFollowRelationsEntry(FollowRelations followRelationsEntry);

        @Query("DELETE FROM follow_relations")
        Completable deleteAllFollowRelationsEntries();

}
