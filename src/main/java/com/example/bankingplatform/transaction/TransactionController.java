package com.example.bankingplatform.transaction;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transaction API", description = "Transaction management endpoints")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transaction by ID")
    public ResponseEntity<TransactionDto> getTransactionById(@PathVariable Long id) {
        Transaction transaction = transactionService.getById(id);
        return ResponseEntity.ok(toTransactionDto(transaction));
    }

    @PostMapping
    @Operation(summary = "Create new transaction")
    public ResponseEntity<TransactionDto> createTransaction(@Valid @RequestBody Transaction transaction) {
        Transaction created = transactionService.createTransaction(transaction);
        return ResponseEntity.ok(toTransactionDto(created));
    }

    @GetMapping
    @Operation(summary = "Get transactions with filters")
    public ResponseEntity<List<TransactionDto>> getTransactions(
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) TransactionStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        
        List<Transaction> transactions = transactionService.findWithFilters(accountId, customerId, status, from, to);
        return ResponseEntity.ok(transactions.stream()
                .map(this::toTransactionDto)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}/context")
    @Operation(summary = "Get transaction context for fraud detection")
    public ResponseEntity<TransactionContextDto> getTransactionContext(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.getTransactionContext(id));
    }

    private TransactionDto toTransactionDto(Transaction transaction) {
        return new TransactionDto(
                transaction.getId(),
                transaction.getAccount().getId(),
                transaction.getTransactionType(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getMerchantName(),
                transaction.getMerchantCategory(),
                transaction.getCountry(),
                transaction.getCity(),
                transaction.getTransactionTimestamp(),
                transaction.getStatus(),
                transaction.getDescription(),
                transaction.getCreatedAt()
        );
    }
}