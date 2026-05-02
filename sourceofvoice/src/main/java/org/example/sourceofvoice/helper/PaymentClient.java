package org.example.sourceofvoice.helper;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Service
public class PaymentClient {

    private final WebClient webClient;
    private final String internalSecret;

    public PaymentClient(
            WebClient.Builder builder,
            @Value("${payment.service-url}") String paymentServiceUrl,
            @Value("${payment.internal-secret}") String internalSecret
    ) {
        this.webClient = builder
                .baseUrl(paymentServiceUrl)
                .build();

        this.internalSecret = internalSecret;
    }

    public Mono<Void> rewardAudio(
            Long userId,
            Long audioSubmissionId,
            BigDecimal amount
    ) {
        RewardAudioPaymentRequest request = new RewardAudioPaymentRequest();

        request.setUserId(userId);
        request.setAudioSubmissionId(audioSubmissionId);
        request.setAmount(amount);
        request.setDescription("Reward for approved audio submission #" + audioSubmissionId);

        return webClient.post()
                .uri("/payments/internal/audio-rewards")
                .header("X-Internal-Secret", internalSecret)
                .bodyValue(request)
                .retrieve()
                .toBodilessEntity()
                .then();
    }

    @Data
    public static class RewardAudioPaymentRequest {
        private Long userId;
        private Long audioSubmissionId;
        private BigDecimal amount;
        private String description;
    }
}