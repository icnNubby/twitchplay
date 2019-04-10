package ru.nubby.playstream.data.repositories.users;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Completable;
import io.reactivex.Single;
import ru.nubby.playstream.data.sources.database.LocalRepository;
import ru.nubby.playstream.data.sources.twitchapi.RemoteRepository;
import ru.nubby.playstream.domain.UsersRepository;
import ru.nubby.playstream.domain.entities.ChannelInfoV5;
import ru.nubby.playstream.domain.entities.ChannelPanel;
import ru.nubby.playstream.domain.entities.Game;
import ru.nubby.playstream.domain.entities.Stream;
import ru.nubby.playstream.domain.entities.UserData;

@Singleton
public class UsersRepositoryImpl implements UsersRepository {

    private final LocalRepository mLocalRepository;
    private final RemoteRepository mRemoteRepository;

    @Inject
    public UsersRepositoryImpl(LocalRepository localRepository,
                               RemoteRepository remoteRepository) {
        mLocalRepository = localRepository;
        mRemoteRepository = remoteRepository;
    }

    @Override
    public Single<UserData> getUserFromStreamer(Stream stream) {
        return mLocalRepository
                .findUserDataById(stream.getUserId())
                .switchIfEmpty(mRemoteRepository
                        .getStreamerInfo(stream)
                        .flatMap(userData ->
                                mLocalRepository
                                        .insertUserData(userData)
                                        .andThen(Single.just(userData))));
    }

    @Override
    public Single<UserData> getUserFromToken(String token) {
        return mRemoteRepository
                .getUserDataFromToken(token)
                .flatMap(userData ->
                        mLocalRepository
                                .insertUserData(userData)
                                .andThen(Single.just(userData)));
    }

    @Override
    public Completable synchronizeUserData() {
        return mLocalRepository
                .getAllUserDataEntries()
                .toSingle()
                .flatMap(mRemoteRepository::getUpdatedUserDataList)
                .flatMapCompletable(updatedUserDataList -> mLocalRepository
                        .insertUserDataList(updatedUserDataList.toArray(new UserData[0])));
    }

    @Override
    public Single<List<UserData>> getUsersByIds(List<String> usersIds) {
        final ArrayList<String> locallyFetched = new ArrayList<>();
        Single<List<UserData>> local = mLocalRepository
                .findUserDataByIdList(usersIds)
                .toSingle()
                .doOnSuccess(users -> {
                    for (UserData item : users) {
                        if (item != null) {
                            locallyFetched.add(item.getId());
                        }
                    }
                });

        Single<List<UserData>> remote =
                Single.defer(() -> mRemoteRepository
                        .getUserDataListByStreamList(getUnfetchedIds(usersIds, locallyFetched)))
                        .flatMap(userData ->
                                mLocalRepository
                                        .insertUserDataList(userData.toArray(new UserData[0]))
                                        .andThen(Single.just(userData)));

        return local
                .concatWith(remote)
                .flatMapIterable(items -> items)
                .toObservable()
                .toList();
    }

    @Override
    public Single<List<ChannelPanel>> getPanelsForUser(String userId) {
        return mRemoteRepository.getPanelsForUser(userId);
    }

    @Override
    public Single<ChannelInfoV5> getOldChannelInfo(String userId) {
        return mRemoteRepository.getChannelInfoV5(userId);
    }

    private List<String> getUnfetchedIds(List<String> allIds, List<String> alreadyFetched) {

        HashMap<String, Void> allIdsMapped = new HashMap<>();
        for (String id : allIds) {
            allIdsMapped.put(id, null);
        }
        for (String id : alreadyFetched) {
            allIdsMapped.remove(id);
        }
        return new ArrayList<>(allIdsMapped.keySet());

    }
}
