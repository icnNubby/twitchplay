package ru.nubby.playstream.domain.interactors;

import java.util.List;

import io.reactivex.Single;
import ru.nubby.playstream.domain.UsersRepository;
import ru.nubby.playstream.domain.entities.ChannelPanel;
import ru.nubby.playstream.domain.entities.UserData;

public class PanelsInteractor {
    private final UsersRepository mUsersRepository;

    public PanelsInteractor(UsersRepository usersRepository) {
        mUsersRepository = usersRepository;
    }

    public Single<List<ChannelPanel>> getPanelsForUser(UserData user) {
        return mUsersRepository.getPanelsForUser(user.getId());
    }
}
