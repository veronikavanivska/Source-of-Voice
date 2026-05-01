package org.example.sourceofvoice.repositories;

import org.example.sourceofvoice.entities.AudioText;
import org.example.sourceofvoice.entities.AudioTextStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface AudioTextRepository extends ReactiveCrudRepository<AudioText,Long> {

    Mono<Boolean> existsBySourcePageIdAndLanguageCode(Long sourcePageId, String languageCode);
    Flux<AudioText> findAllBy(Pageable pageable);
    Flux<AudioText> findByStatus(
            AudioTextStatus status,
            Pageable pageable
    );

    Flux<AudioText> findByLanguageCodeAndStatus(
            String languageCode,
            AudioTextStatus status,
            Pageable pageable
    );

    Mono<AudioText> findByIdAndStatus(
            Long id,
            AudioTextStatus status
    );
}
