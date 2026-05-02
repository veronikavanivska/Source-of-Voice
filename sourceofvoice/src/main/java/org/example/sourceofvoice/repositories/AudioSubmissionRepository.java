package org.example.sourceofvoice.repositories;

import org.example.sourceofvoice.entities.audio.AudioSubmission;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AudioSubmissionRepository extends ReactiveCrudRepository<AudioSubmission, Long> {
}
