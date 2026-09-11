package com.example.bankingplatform.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccountId(Long accountId);

    List<Transaction> findByStatus(TransactionStatus status);

    List<Transaction> findByTransactionTimestampBetween(LocalDateTime from, LocalDateTime to);

    @Query("SELECT t FROM Transaction t WHERE t.account.customer.id = :customerId")
    List<Transaction> findByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT t FROM Transaction t WHERE (:accountId IS NULL OR t.account.id = :accountId) " +
           "AND (:customerId IS NULL OR t.account.customer.id = :customerId) " +
           "AND (:status IS NULL OR t.status = :status) " +
           "AND (:from IS NULL OR t.transactionTimestamp >= :from) " +
           "AND (:to IS NULL OR t.transactionTimestamp <= :to)")
    List<Transaction> findWithFilters(
            @Param("accountId") Long accountId,
            @Param("customerId") Long customerId,
            @Param("status") TransactionStatus status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}