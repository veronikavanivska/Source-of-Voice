package org.example.payment.service;

import org.example.payment.DTO.request.RewardAudioPaymentRequest;
import org.example.payment.DTO.response.SliceResponse;
import org.example.payment.DTO.response.WalletResponse;
import org.example.payment.DTO.response.WalletTransactionResponse;
import org.example.payment.entities.Wallet;
import org.example.payment.entities.WalletTransaction;
import org.example.payment.entities.WalletTransactionStatus;
import org.example.payment.entities.WalletTransactionType;
import org.example.payment.repositories.WalletRepository;
import org.example.payment.repositories.WalletTransactionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class WalletService {

    private static final String SOURCE_SERVICE_SOURCE_OF_VOICE = "sourceofvoice";

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final DatabaseClient databaseClient;
    private final TransactionalOperator transactionalOperator;
    private final BigDecimal maxAudioReward;

    public WalletService(
            WalletRepository walletRepository,
            WalletTransactionRepository walletTransactionRepository,
            DatabaseClient databaseClient,
            TransactionalOperator transactionalOperator,
            @Value("${payment.max-audio-reward:100.00}") BigDecimal maxAudioReward
    ) {
        this.walletRepository = walletRepository;
        this.walletTransactionRepository = walletTransactionRepository;
        this.databaseClient = databaseClient;
        this.transactionalOperator = transactionalOperator;
        this.maxAudioReward = maxAudioReward;
    }

    public Mono<WalletResponse> getWallet(Long userId) {
        return getOrCreateWallet(userId)
                .map(this::toWalletResponse);
    }

    public Mono<WalletResponse> getOfCreateWallet(Long userId){
        return getOrCreateWallet(userId)
                .map(this::toWalletResponse);
    }

    public Mono<SliceResponse<WalletTransactionResponse>> getTransactions(Long userId, int page, int size){
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);

        PageRequest pageRequest = PageRequest.of(
                safePage,
                safeSize + 1,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return walletTransactionRepository.findByUserId(userId,pageRequest)
                .map(this::toTransactionResponse)
                .collectList()
                .map(items-> SliceResponse.of(items,safePage,safeSize));
    }

    public Mono<WalletTransactionResponse> rewardForAudio(RewardAudioPaymentRequest request){
        validateRewardRequest(request);

        String sourceReferenceId = String.valueOf(request.getAudioSubmissionId());

        return transactionalOperator.transactional(
                        ensureWalletExists(request.getUserId())
                                .then(createPendingRewardTransaction(request, sourceReferenceId))
                                .flatMap(transaction ->
                                        increaseWalletBalanceAtomically(
                                                request.getUserId(),
                                                request.getAmount()
                                        )
                                                .flatMap(updatedWallet ->
                                                        completeTransaction(
                                                                transaction.getId(),
                                                                updatedWallet.getBalance()
                                                        )
                                                )
                                )
                )
                .onErrorMap(DataIntegrityViolationException.class, error ->
                        new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "Payment for this audio submission already exists"
                        )
                )
                .map(this::toTransactionResponse);
    }


    private Mono<Wallet> increaseWalletBalanceAtomically(
            Long userId,
            BigDecimal amount
    ) {
        return databaseClient.sql("""
                        UPDATE wallets
                        SET balance = balance + :amount,
                            updated_at = NOW()
                        WHERE user_id = :userId
                        RETURNING id, user_id, balance, created_at, updated_at
                        """)
                .bind("amount", amount)
                .bind("userId", userId)
                .map((row, metadata) -> Wallet.builder()
                        .id(row.get("id", Long.class))
                        .userId(row.get("user_id", Long.class))
                        .balance(row.get("balance", BigDecimal.class))
                        .createdAt(row.get("created_at", LocalDateTime.class))
                        .updatedAt(row.get("updated_at", LocalDateTime.class))
                        .build()
                )
                .one()
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Wallet balance update failed"
                )));
    }

    private Mono<WalletTransaction> completeTransaction(
            Long transactionId,
            BigDecimal balanceAfter
    ) {
        return databaseClient.sql("""
                        UPDATE wallet_transactions
                        SET status = 'COMPLETED',
                            balance_after = :balanceAfter
                        WHERE id = :transactionId
                        RETURNING id, wallet_id, user_id, type, status, amount,
                                  balance_after, source_service, source_reference_id,
                                  description, created_at
                        """)
                .bind("balanceAfter", balanceAfter)
                .bind("transactionId", transactionId)
                .map((row, metadata) -> WalletTransaction.builder()
                        .id(row.get("id", Long.class))
                        .walletId(row.get("wallet_id", Long.class))
                        .userId(row.get("user_id", Long.class))
                        .type(WalletTransactionType.valueOf(row.get("type", String.class)))
                        .status(WalletTransactionStatus.valueOf(row.get("status", String.class)))
                        .amount(row.get("amount", BigDecimal.class))
                        .balanceAfter(row.get("balance_after", BigDecimal.class))
                        .sourceService(row.get("source_service", String.class))
                        .sourceReferenceId(row.get("source_reference_id", String.class))
                        .description(row.get("description", String.class))
                        .createdAt(row.get("created_at", LocalDateTime.class))
                        .build()
                )
                .one()
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Transaction completion failed"
                )));
    }


    private Mono<WalletTransaction> createPendingRewardTransaction(RewardAudioPaymentRequest request, String sourceReferenceId){
        LocalDateTime now = LocalDateTime.now();

        WalletTransaction transaction = WalletTransaction.builder()
                .walletId(null)
                .userId(request.getUserId())
                .type(WalletTransactionType.AUDIO_REWARD)
                .status(WalletTransactionStatus.PENDING)
                .amount(request.getAmount())
                .balanceAfter(BigDecimal.ZERO)
                .sourceService(SOURCE_SERVICE_SOURCE_OF_VOICE)
                .sourceReferenceId(sourceReferenceId)
                .description(request.getDescription())
                .createdAt(now)
                .build();

        return walletRepository.findByUserId(request.getUserId())
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Wallet was not created"
                )))
                .flatMap(wallet -> {
                    transaction.setWalletId(wallet.getId());
                    return walletTransactionRepository.save(transaction);
                });
    }


    private void validateRewardRequest(RewardAudioPaymentRequest request) {
        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Request body is required"
            );
        }

        if (request.getUserId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "userId is required"
            );
        }

        if (request.getAudioSubmissionId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "audioSubmissionId is required"
            );
        }

        if (request.getAmount() == null || request.getAmount().signum() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "amount must be positive"
            );
        }

        if (request.getAmount().compareTo(maxAudioReward) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "amount exceeds maximum allowed audio reward"
            );
        }
    }

    private Mono<Void> ensureWalletExists(Long userId) {
        return databaseClient.sql("""
                        INSERT INTO wallets (user_id, balance, created_at, updated_at)
                        VALUES (:userId, 0.00, NOW(), NOW())
                        ON CONFLICT (user_id) DO NOTHING
                        """)
                .bind("userId", userId)
                .fetch()
                .rowsUpdated()
                .then();
    }

    private Mono<Wallet> getOrCreateWallet(Long userId) {
        return ensureWalletExists(userId)
                .then(walletRepository.findByUserId(userId));
    }

    private WalletResponse toWalletResponse(Wallet wallet) {
        WalletResponse response = new WalletResponse();

        response.setWalletId(wallet.getId());
        response.setUserId(wallet.getUserId());
        response.setBalance(wallet.getBalance());
        response.setCreatedAt(wallet.getCreatedAt());
        response.setUpdatedAt(wallet.getUpdatedAt());

        return response;
    }

    private WalletTransactionResponse toTransactionResponse(
            WalletTransaction transaction
    ) {
        WalletTransactionResponse response = new WalletTransactionResponse();

        response.setId(transaction.getId());
        response.setWalletId(transaction.getWalletId());
        response.setUserId(transaction.getUserId());
        response.setType(transaction.getType());
        response.setStatus(transaction.getStatus());
        response.setAmount(transaction.getAmount());
        response.setBalanceAfter(transaction.getBalanceAfter());
        response.setSourceService(transaction.getSourceService());
        response.setSourceReferenceId(transaction.getSourceReferenceId());
        response.setDescription(transaction.getDescription());
        response.setCreatedAt(transaction.getCreatedAt());

        return response;
    }
}

