package ru.nubby.playstream.domain.interactors;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Single;
import ru.nubby.playstream.domain.UsersRepository;
import ru.nubby.playstream.domain.entities.ChannelPanel;
import ru.nubby.playstream.domain.entities.UserData;

/**
 * Business logic for retrieving panels of user.
 */
@Singleton
public class PanelsInteractor {
    private final UsersRepository mUsersRepository;

    @Inject
    public PanelsInteractor(UsersRepository usersRepository) {
        mUsersRepository = usersRepository;
    }

    /**
     * Gets panels, attached to user.
     * @param user user to lookup for.
     * @return list of panels.
     */
    public Single<List<ChannelPanel>> getPanelsForUser(UserData user) {
        return mUsersRepository.getPanelsForUser(user.getLogin());
    }
}
