package org.example.payment.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("wallet_transactions")
public class WalletTransaction {

    @Id
    private Long id;

    @Column("wallet_id")
    private Long walletId;

    @Column("user_id")
    private Long userId;

    @Column("type")
    private WalletTransactionType type;

    @Column("status")
    private WalletTransactionStatus status;

    @Column("amount")
    private BigDecimal amount;

    @Column("balance_after")
    private BigDecimal balanceAfter;

    @Column("source_service")
    private String sourceService;

    @Column("source_reference_id")
    private String sourceReferenceId;

    @Column("description")
    private String description;

    @Column("created_at")
    private LocalDateTime createdAt;
}
