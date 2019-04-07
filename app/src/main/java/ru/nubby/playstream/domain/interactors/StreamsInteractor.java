package ru.nubby.playstream.domain.interactors;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Single;
import ru.nubby.playstream.domain.StreamsRepository;
import ru.nubby.playstream.domain.entities.QualityLinks;
import ru.nubby.playstream.domain.entities.Stream;

/**
 * Business logic for streams.
 */
@Singleton
public class StreamsInteractor {

    private final StreamsRepository mStreamsRepository;

    @Inject
    public StreamsInteractor(StreamsRepository streamsRepository) {
        mStreamsRepository = streamsRepository;
    }

    public Single<QualityLinks> getStreamLinks(Stream stream) {
        return
                mStreamsRepository
                        .getQualityUrls(stream)
                        .map(qualityMap -> new QualityLinks(stream, qualityMap));
    }
}
