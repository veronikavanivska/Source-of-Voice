package org.example.sourceofvoice.repositories;

import org.example.sourceofvoice.entities.AudioTextBatch;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;


@Repository
public interface AudioTextBatchRepository extends ReactiveCrudRepository<AudioTextBatch, Long> {

    Flux<AudioTextBatch> findAllBy(Pageable pageable);
}
