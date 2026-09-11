package com.example.bankingplatform.account;

import com.example.bankingplatform.transaction.TransactionDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public Account createAccount(Account account) {
        log.info("Creating new account: {}", account.getAccountNumber());
        return accountRepository.save(account);
    }

    @Transactional(readOnly = true)
    public Account getById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + id));
    }

    @Transactional
    public Account updateAccount(Long id, Account updatedAccount) {
        Account account = getById(id);
        account.setAccountType(updatedAccount.getAccountType());
        account.setCurrency(updatedAccount.getCurrency());
        account.setBalance(updatedAccount.getBalance());
        account.setStatus(updatedAccount.getStatus());
        log.info("Updated account: {}", id);
        return accountRepository.save(account);
    }

    @Transactional
    public void deleteAccount(Long id) {
        Account account = getById(id);
        accountRepository.delete(account);
        log.info("Deleted account: {}", id);
    }

    @Transactional(readOnly = true)
    public List<Account> findAll() {
        return accountRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<TransactionDto> getAccountTransactions(Long accountId) {
        getById(accountId);
        return accountRepository.findById(accountId)
                .map(account -> account.getTransactions().stream()
                        .map(transaction -> new TransactionDto(
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
                                transaction.getCreatedAt()))
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }
}