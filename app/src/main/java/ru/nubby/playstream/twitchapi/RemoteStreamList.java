package ru.nubby.playstream.twitchapi;

import java.util.List;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.nubby.playstream.SensitiveStorage;
import ru.nubby.playstream.model.Pagination;
import ru.nubby.playstream.model.StreamsRequest;
import ru.nubby.playstream.model.Stream;

public class RemoteStreamList implements Repository {
    @Override
    public Single<StreamsRequest> getStreams() {
        return TwitchApi
                .getInstance()
                .getStreamHelixService()
                .getTopStreams(SensitiveStorage.getClientApiKey())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Single<StreamsRequest> getStreams(Pagination pagination) {
        return TwitchApi
                .getInstance()
                .getStreamHelixService()
                .getMoreStreamsAfter(SensitiveStorage.getClientApiKey(), pagination.getCursor())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
