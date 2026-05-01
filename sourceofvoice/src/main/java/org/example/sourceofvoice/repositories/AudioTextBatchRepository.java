package org.example.sourceofvoice.repositories;

import org.example.sourceofvoice.entities.AudioTextBatch;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AudioTextBatchRepository extends ReactiveCrudRepository<AudioTextBatch, Long> {
}
