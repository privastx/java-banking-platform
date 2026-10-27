package com.example.bankingplatform.ai.agent.tools;

import com.example.bankingplatform.transaction.Transaction;
import com.example.bankingplatform.transaction.TransactionRepository;
import dev.langchain4j.agent.tool.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GetRecentTransactionsTool {

    private static final Logger log = LoggerFactory.getLogger(GetRecentTransactionsTool.class);

    private final TransactionRepository transactionRepository;

    public GetRecentTransactionsTool(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Tool("Get the 3 most recent transactions for a customer")
    public List<TransactionSummary> getRecentTransactions(Long customerId) {
        log.info("Getting recent transactions for customerId={}", customerId);

        if (customerId == null || customerId <= 0) {
            return List.of();
        }

        try {
            List<Transaction> transactions = transactionRepository.findByCustomerId(customerId);

            List<TransactionSummary> result = transactions.stream()
                    .sorted(Comparator.comparing(Transaction::getTransactionTimestamp).reversed())
                    .limit(3)
                    .map(tx -> new TransactionSummary(
                            tx.getMerchantName(),
                            tx.getAmount(),
                            tx.getCurrency(),
                            tx.getTransactionTimestamp()))
                    .collect(Collectors.toList());
            
            return result;
        } catch (Exception e) {
            log.error("Failed to get transactions for customerId={}", customerId, e);
            return List.of();
        }
    }

    public record TransactionSummary(
            String merchantName,
            java.math.BigDecimal amount,
            String currency,
            java.time.LocalDateTime transactionTimestamp
    ) {}
}