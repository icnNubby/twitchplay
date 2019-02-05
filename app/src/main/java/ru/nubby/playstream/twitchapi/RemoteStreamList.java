package ru.nubby.playstream.twitchapi;

import java.util.List;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.nubby.playstream.SensitiveStorage;
import ru.nubby.playstream.model.StreamsRequest;
import ru.nubby.playstream.model.Stream;

public class RemoteStreamList implements Repository {
    @Override
    public Single<List<Stream>> getStreams() {
        return TwitchApi
                .getInstance()
                .getStreamHelixService()
                .getTopStreams(SensitiveStorage.getClientApiKey())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(StreamsRequest::getData);
    }
}
