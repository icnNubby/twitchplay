package ru.nubby.playstream.domain.interactors;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.Single;
import ru.nubby.playstream.domain.StreamsRepository;
import ru.nubby.playstream.domain.entities.Pagination;
import ru.nubby.playstream.domain.entities.QualityLinks;
import ru.nubby.playstream.domain.entities.Stream;
import ru.nubby.playstream.domain.entities.StreamsResponse;

/**
 * Business logic for streams.
 */
@Singleton
public class StreamsInteractor {

    private final StreamsRepository mStreamsRepository;
    private final AuthInteractor mAuthInteractor;

    @Inject
    public StreamsInteractor(StreamsRepository streamsRepository,
                             AuthInteractor authInteractor) {
        mStreamsRepository = streamsRepository;
        mAuthInteractor = authInteractor;
    }

    public Single<QualityLinks> getStreamLinks(Stream stream) {
        return mStreamsRepository
                .getQualityUrls(stream)
                .map(qualityMap -> new QualityLinks(stream, qualityMap));
    }

    public Single<StreamsResponse> getTopStreams() {
        return mStreamsRepository
                .getTopStreams();
    }

    public Single<StreamsResponse> getTopStreams(Pagination pagination) {
        return mStreamsRepository
                .getTopStreams(pagination);
    }

    public Single<List<Stream>> getLiveStreamsFollowedByUser() {
        return mAuthInteractor
                .getCurrentLoginInfo()
                .flatMap(mStreamsRepository::getLiveStreamsFollowedByUser);
    }

    public Observable<Stream> getUpdatableStreamInfo(Stream stream) {
        return mStreamsRepository
                .getUpdatableStreamInfo(stream);
    }

}
