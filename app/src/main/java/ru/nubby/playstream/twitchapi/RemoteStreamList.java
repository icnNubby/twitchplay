package ru.nubby.playstream.twitchapi;

import android.util.Log;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import ru.nubby.playstream.SensitiveStorage;
import ru.nubby.playstream.model.FollowRelations;
import ru.nubby.playstream.model.Pagination;
import ru.nubby.playstream.model.StreamsRequest;
import ru.nubby.playstream.model.UserFollowsRequest;

public class RemoteStreamList implements Repository {
    private final String TAG = RemoteStreamList.class.getSimpleName();

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

    public Single<List<FollowRelations>> getUserFollows(String userId) {

        return Observable
                .defer(() ->
                {
                    Log.d(TAG, "Combining observables");
                    BehaviorSubject<String> pagecontrol = BehaviorSubject.create();
                    pagecontrol.onNext("start");
                    return pagecontrol.concatMap(aKey ->
                    {
                        Single<UserFollowsRequest> listSingle;
                        if (aKey != null && aKey.equals("start")) {
                            listSingle = TwitchApi
                                    .getInstance()
                                    .getStreamHelixService()
                                    .getUserFollowsById(SensitiveStorage.getClientApiKey(), userId);
                            Log.d(TAG, "Emitting start one");
                            return listSingle.doOnSuccess(page -> {
                                if (page.getPagination() != null && page.getPagination().getCursor() != null)
                                    pagecontrol.onNext(page.getPagination().getCursor());
                                else Observable.<UserFollowsRequest>empty()
                                        .doOnComplete(pagecontrol::onComplete);
                            })
                                    .toObservable();
                        } else if (aKey != null) {
                            listSingle = TwitchApi
                                    .getInstance()
                                    .getStreamHelixService()
                                    .getUserFollowsById(SensitiveStorage.getClientApiKey(), userId, aKey);

                            Log.d(TAG, "Emitting next one " + aKey);
                            return listSingle.doOnSuccess(page -> {
                                if (page.getPagination() != null && page.getPagination().getCursor() != null)
                                    pagecontrol.onNext(page.getPagination().getCursor());
                                else Observable.<UserFollowsRequest>empty()
                                        .doOnComplete(pagecontrol::onComplete);
                            })
                                    .toObservable();

                        } else {
                            return Observable.<UserFollowsRequest>empty()
                                    .doOnComplete(pagecontrol::onComplete);
                        }
                    });
                })
                .doOnNext(userFollowsRequest -> Log.d(TAG, userFollowsRequest.toString()))
                .map(UserFollowsRequest::getData)
                .flatMap(Observable::fromIterable)
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

}
