package org.example.payment.DTO.response;

import lombok.Data;
import org.example.payment.entities.WalletTransactionStatus;
import org.example.payment.entities.WalletTransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WalletTransactionResponse {
    private Long id;
    private Long walletId;
    private Long userId;
    private WalletTransactionType type;
    private WalletTransactionStatus status;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String sourceService;
    private String sourceReferenceId;
    private String description;
    private LocalDateTime createdAt;
}
