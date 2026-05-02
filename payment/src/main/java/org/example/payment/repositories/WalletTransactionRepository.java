package org.example.payment.repositories;

import org.example.payment.entities.WalletTransaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface WalletTransactionRepository extends ReactiveCrudRepository<WalletTransaction,Long> {

    Flux<WalletTransaction> findByUserId(Long userId, Pageable pageable);

    Mono<Boolean> existsBySourceServiceAndSourceReferenceId(
            String sourceService,
            String sourceReferenceId
    );
}
