package ru.nubby.playstream.twitchapi;

import android.util.Log;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.nubby.playstream.SensitiveStorage;
import ru.nubby.playstream.model.StreamsRequest;
import ru.nubby.playstream.model.Stream;

public class RemoteStreamList implements Repository {
    @Override
    public Observable<List<Stream>> getStreams() {
        return TwitchApi
                .getInstance()
                .getStreamServiceHelix()
                .getTopStreams(SensitiveStorage.getClientApiKey())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(StreamsRequest::getData);
    }
}
