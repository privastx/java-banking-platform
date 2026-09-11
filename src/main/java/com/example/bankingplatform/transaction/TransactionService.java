package com.example.bankingplatform.transaction;

import com.example.bankingplatform.account.Account;
import com.example.bankingplatform.account.AccountService;
import com.example.bankingplatform.customer.Customer;
import com.example.bankingplatform.customer.CustomerService;
import com.example.bankingplatform.customer.CustomerDto;
import com.example.bankingplatform.account.AccountDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final CustomerService customerService;

    public TransactionService(TransactionRepository transactionRepository,
                              AccountService accountService,
                              CustomerService customerService) {
        this.transactionRepository = transactionRepository;
        this.accountService = accountService;
        this.customerService = customerService;
    }

    @Transactional
    public Transaction createTransaction(Transaction transaction) {
        log.info("Creating new transaction for account: {}", transaction.getAccount().getId());
        return transactionRepository.save(transaction);
    }

    @Transactional(readOnly = true)
    public Transaction getById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Transaction> findByAccountId(Long accountId) {
        return transactionRepository.findByAccountId(accountId);
    }

    @Transactional(readOnly = true)
    public List<Transaction> findByCustomerId(Long customerId) {
        return transactionRepository.findByCustomerId(customerId);
    }

    @Transactional(readOnly = true)
    public List<Transaction> findByStatus(TransactionStatus status) {
        return transactionRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<Transaction> findByDateRange(LocalDateTime from, LocalDateTime to) {
        return transactionRepository.findByTransactionTimestampBetween(from, to);
    }

    @Transactional(readOnly = true)
    public List<Transaction> findWithFilters(Long accountId, Long customerId,
 TransactionStatus status, LocalDateTime from, LocalDateTime to) {
        return transactionRepository.findWithFilters(accountId, customerId, status, from, to);
    }

    @Transactional(readOnly = true)
    public TransactionContextDto getTransactionContext(Long transactionId) {
        Transaction transaction = getById(transactionId);
        Account account = transaction.getAccount();
        Customer customer = account.getCustomer();

        TransactionDto transactionDto = new TransactionDto(
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

        CustomerDto customerDto = new CustomerDto(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getDateOfBirth(),
                customer.getCountry(),
                customer.getStatus(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );

        AccountDto accountDto = new AccountDto(
                account.getId(),
                account.getCustomer().getId(),
                account.getAccountNumber(),
                account.getAccountType(),
                account.getCurrency(),
                account.getBalance(),
                account.getStatus(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );

        return new TransactionContextDto(transactionDto, customerDto, accountDto);
    }
}