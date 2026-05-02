package org.example.sourceofvoice.repositories;

import org.example.sourceofvoice.entities.audio.AudioSubmission;
import org.example.sourceofvoice.entities.audio.AudioSubmissionStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;


@Repository
public interface AudioSubmissionRepository extends ReactiveCrudRepository<AudioSubmission, Long> {

    Flux<AudioSubmission> findByUserId(Long userId, Pageable pageable);

    Flux<AudioSubmission> findByStatus(AudioSubmissionStatus status);

    Flux<AudioSubmission> findByStatus(AudioSubmissionStatus status, Pageable pageable);

    Flux<AudioSubmission> findAllBy(Pageable pageable);

    Mono<AudioSubmission> findBySpeechmaticsJobId(String speechmaticsJobId);

    Flux<AudioSubmission> findByStatusAndAssignedReviewerIdIsNull(
            AudioSubmissionStatus status,
            Pageable pageable
    );

    Flux<AudioSubmission> findByAssignedReviewerIdAndStatus(
            Long assignedReviewerId,
            AudioSubmissionStatus status,
            Pageable pageable
    );

    Mono<Long> countByAudioTextIdAndStatusIn(
            Long audioTextId,
            Collection<AudioSubmissionStatus> statuses
    );
}
