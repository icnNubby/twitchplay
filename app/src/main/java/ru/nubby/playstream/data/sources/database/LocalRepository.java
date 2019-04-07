package ru.nubby.playstream.data.sources.database;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import ru.nubby.playstream.domain.entities.FollowRelations;
import ru.nubby.playstream.domain.entities.Game;
import ru.nubby.playstream.domain.entities.UserData;

public interface LocalRepository {

    //Follow relations

    Single<List<FollowRelations>> getFollowRelationsEntriesById(String fromId);

    Single<List<FollowRelations>> findRelation(String fromId, String toId);

    Completable insertFollowRelationsEntry(FollowRelations followRelationsEntry);

    Completable insertFollowRelationsList(FollowRelations... followRelationsList);

    Completable deleteFollowRelationsEntry(FollowRelations followRelationsEntry);

    Completable deleteAllFollowRelationsEntries();

    //UserData

    Maybe<UserData> findUserDataById(String id);

    Maybe<List<UserData>> findUserDataByIdList(List<String> id);

    Maybe<List<UserData>> getAllUserDataEntries();

    Completable insertUserData(UserData userDataEntry);

    Completable insertUserDataList(UserData... userDataEntryList);

    Completable deleteUserDataEntry(UserData userDataEntry);

    Completable deleteUserDataEntries();

    //Games

    Maybe<Game> findGame(String id);

    Maybe<List<Game>> findGames(List<String > ids);

    Completable insertGame(Game game);

    Completable insertGameList(Game... game);

    Completable deleteGame(Game game);

    Completable deleteAllGames();

}
