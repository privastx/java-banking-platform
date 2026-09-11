package com.example.bankingplatform.account;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void createAccount_shouldSaveAndReturnAccount() {
        Account account = new Account();
        account.setAccountNumber("ACC-TEST-001");
        account.setAccountType(AccountType.CHECKING);
        account.setBalance(BigDecimal.valueOf(1000));

        when(accountRepository.save(any(Account.class))).thenReturn(account);

        Account result = accountService.createAccount(account);

        assertNotNull(result);
        assertEquals("ACC-TEST-001", result.getAccountNumber());
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    void getById_shouldReturnAccountWhenExists() {
        Account account = new Account();
        account.setId(1L);
        account.setAccountNumber("ACC-001");

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        Account result = accountService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getById_shouldThrowExceptionWhenNotFound() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            accountService.getById(99L);
        });

        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void updateAccount_shouldUpdateAndReturnAccount() {
        Account existingAccount = new Account();
        existingAccount.setId(1L);
        existingAccount.setAccountNumber("ACC-001");
        existingAccount.setBalance(BigDecimal.valueOf(1000));

        Account updateRequest = new Account();
        updateRequest.setBalance(BigDecimal.valueOf(2000));
        updateRequest.setStatus(AccountStatus.FROZEN);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(existingAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(existingAccount);

        Account result = accountService.updateAccount(1L, updateRequest);

        assertEquals(BigDecimal.valueOf(2000), result.getBalance());
        assertEquals(AccountStatus.FROZEN, result.getStatus());
    }

    @Test
    void findAll_shouldReturnAllAccounts() {
        Account account = new Account();
        account.setId(1L);

        when(accountRepository.findAll()).thenReturn(java.util.List.of(account));

        var result = accountService.findAll();

        assertEquals(1, result.size());
    }
}