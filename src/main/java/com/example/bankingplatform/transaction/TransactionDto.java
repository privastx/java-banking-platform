package com.example.bankingplatform.transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDto {
    private Long id;
    private Long accountId;
    private TransactionType transactionType;
    private BigDecimal amount;
    private String currency;
    private String merchantName;
    private MerchantCategory merchantCategory;
    private String country;
    private String city;
    private LocalDateTime transactionTimestamp;
    private TransactionStatus status;
    private String description;
    private LocalDateTime createdAt;
}
