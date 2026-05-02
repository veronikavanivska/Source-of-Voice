package org.example.payment.repositories;

import org.example.payment.entities.Wallet;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface WalletRepository extends ReactiveCrudRepository<Wallet, Long> {
    Mono<Wallet> findByUserId(Long userId);
}

