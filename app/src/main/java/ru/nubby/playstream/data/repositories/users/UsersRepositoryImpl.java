package ru.nubby.playstream.data.repositories.users;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Completable;
import io.reactivex.Single;
import ru.nubby.playstream.domain.UsersRepository;
import ru.nubby.playstream.data.sources.database.LocalRepository;
import ru.nubby.playstream.data.sources.twitchapi.RemoteRepository;
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

    public Single<UserData> getUserFromToken(String token) {
        return mRemoteRepository
                .getUserDataFromToken(token)
                .flatMap(userData ->
                        mLocalRepository
                                .insertUserData(userData)
                                .andThen(Single.just(userData)));
    }

    public Completable synchronizeUserData() {
        return mLocalRepository
                .getAllUserDataEntries()
                .toSingle()
                .flatMap(mRemoteRepository::getUpdatedUserDataList)
                .flatMapCompletable(updatedUserDataList -> mLocalRepository
                        .insertUserDataList(updatedUserDataList.toArray(new UserData[0])));
    }
}
