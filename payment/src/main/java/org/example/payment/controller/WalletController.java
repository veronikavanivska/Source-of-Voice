package org.example.payment.controller;

import org.example.payment.DTO.request.RewardAudioPaymentRequest;
import org.example.payment.DTO.response.SliceResponse;
import org.example.payment.DTO.response.WalletResponse;
import org.example.payment.DTO.response.WalletTransactionResponse;
import org.example.payment.service.WalletService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/payments")
public class WalletController {

    private final WalletService walletService;
    private final String internalApiSecret;

    public WalletController(
            WalletService walletService,
            @Value("${internal.api.secret}") String internalApiSecret
    ) {
        this.walletService = walletService;
        this.internalApiSecret = internalApiSecret;
    }

    @GetMapping("/wallet/me")
    public Mono<WalletResponse> getMyWallet(
            @RequestHeader("X-User-Id") Long userId
    ) {
        return walletService.getWallet(userId);
    }

    @GetMapping("/wallet/me/transactions")
    public Mono<SliceResponse<WalletTransactionResponse>> getMyTransactions(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return walletService.getTransactions(userId, page, size);
    }

    @PostMapping("/internal/audio-rewards")
    public Mono<WalletTransactionResponse> rewardForAudio(
            @RequestHeader("X-Internal-Secret") String internalSecret,
            @RequestBody RewardAudioPaymentRequest request
    ) {
        validateInternalSecret(internalSecret);

        return walletService.rewardForAudio(request);
    }

    private void validateInternalSecret(String internalSecret) {
        if (internalSecret == null || internalSecret.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Access denied"
            );
        }

        if (!internalApiSecret.equals(internalSecret)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Access denied"
            );
        }
    }
}