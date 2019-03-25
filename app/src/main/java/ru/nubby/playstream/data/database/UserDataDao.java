package ru.nubby.playstream.data.database;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import ru.nubby.playstream.model.UserData;

@Dao
public interface UserDataDao {

    @Query("SELECT * FROM user_data WHERE id = :id")
    Maybe<UserData> findUserDataById(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertUserData(UserData userDataEntry);

    @Query("SELECT * FROM user_data")
    Maybe<List<UserData>> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertUserDataList(UserData... userDataEntry);

    @Delete
    Completable deleteUserDataEntry(UserData userDataEntry);

    @Query("DELETE FROM user_data")
    Completable deleteUserDataEntries();

}
