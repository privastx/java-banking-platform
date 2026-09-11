package com.example.bankingplatform.transaction;

import com.example.bankingplatform.account.Account;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "transactions")
@Getter
@Setter
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Transaction type is required")
    @Column(nullable = false)
    private TransactionType transactionType;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Column(nullable = false)
    private String currency;

    @Column
    private String merchantName;

    @Enumerated(EnumType.STRING)
    @Column
    private MerchantCategory merchantCategory;

    @Column
    private String country;

    @Column
    private String city;

    @Column(name = "transaction_timestamp")
    private LocalDateTime transactionTimestamp;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Status is required")
    @Column(nullable = false)
    private TransactionStatus status;

    @Column
    private String description;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (transactionTimestamp == null) {
            transactionTimestamp = LocalDateTime.now();
        }
        if (status == null) {
            status = TransactionStatus.PENDING;
        }
        if (currency == null) {
            currency = "USD";
        }
    }
}