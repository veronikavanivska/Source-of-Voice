package org.example.sourceofvoice.repositories;

import org.example.sourceofvoice.entities.AudioText;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface AudioTextRepository extends ReactiveCrudRepository<AudioText,Long> {

    Mono<Boolean> existsBySourcePageIdAndLanguageCode(Long sourcePageId, String languageCode); 
    
}
