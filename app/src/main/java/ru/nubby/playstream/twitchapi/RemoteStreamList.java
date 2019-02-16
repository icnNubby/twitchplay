package ru.nubby.playstream.twitchapi;

import android.util.Log;

import java.util.Comparator;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import ru.nubby.playstream.SensitiveStorage;
import ru.nubby.playstream.model.FollowRelations;
import ru.nubby.playstream.model.Pagination;
import ru.nubby.playstream.model.Stream;
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

    @Override
    public Single<List<FollowRelations>> getUserFollows(String userId) {

        return  getAllUserFollowRelations(userId)
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Single<List<Stream>> getLiveStreamsFollowedByUser(String userId) {
        return getAllUserFollowRelations(userId)
                .subscribeOn(Schedulers.io())
                .map(FollowRelations::getToId)
                .buffer(100)
                .flatMap(userList -> TwitchApi
                        .getInstance()
                        .getStreamHelixService()
                        .getAllStreamsByUserList(SensitiveStorage.getClientApiKey(), userList)
                        .toObservable())
                .subscribeOn(Schedulers.computation())
                .map(StreamsRequest::getData)
                .flatMap(Observable::fromIterable)
                .toSortedList()
                .observeOn(AndroidSchedulers.mainThread());
    }

    private Observable<FollowRelations> getAllUserFollowRelations(String userId) {
        return Observable
                .defer(() ->
                {
                    BehaviorSubject<String> pagecontrol = BehaviorSubject.create();
                    pagecontrol.onNext("start");
                    return pagecontrol.concatMap(aKey ->
                    {
                        if (aKey != null && aKey.equals("start")) {
                            return TwitchApi
                                    .getInstance()
                                    .getStreamHelixService()
                                    .getUserFollowsById(SensitiveStorage.getClientApiKey(), userId)
                                    .doOnSuccess(page -> {
                                        if (page.getPagination() != null &&
                                                page.getPagination().getCursor() != null)
                                            pagecontrol.onNext(page.getPagination().getCursor());
                                        else pagecontrol.onComplete();
                                    })
                                    .toObservable();
                        } else if (aKey != null) {
                            return TwitchApi
                                    .getInstance()
                                    .getStreamHelixService()
                                    .getUserFollowsById(SensitiveStorage.getClientApiKey(), userId, aKey)
                                    .doOnSuccess(page -> {
                                        if (page.getPagination() != null &&
                                                page.getPagination().getCursor() != null)
                                            pagecontrol.onNext(page.getPagination().getCursor());
                                        else pagecontrol.onComplete();
                                    })
                                    .toObservable();

                        } else {
                            return Observable.<UserFollowsRequest>empty()
                                    .doOnComplete(pagecontrol::onComplete);
                        }
                    });
                })
                .map(UserFollowsRequest::getData)
                .flatMap(Observable::fromIterable);
    }

}
