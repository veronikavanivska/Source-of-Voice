package org.example.payment.DTO.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RewardAudioPaymentRequest {
    private Long userId;
    private Long audioSubmissionId;
    private BigDecimal amount;
    private String description;
}
