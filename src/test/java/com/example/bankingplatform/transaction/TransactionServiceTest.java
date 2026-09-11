package com.example.bankingplatform.transaction;

import com.example.bankingplatform.account.Account;
import com.example.bankingplatform.account.AccountService;
import com.example.bankingplatform.account.AccountType;
import com.example.bankingplatform.customer.Customer;
import com.example.bankingplatform.customer.CustomerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountService accountService;

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void createTransaction_shouldSaveAndReturnTransaction() {
        Transaction transaction = new Transaction();
        transaction.setAmount(BigDecimal.valueOf(100));
        transaction.setTransactionType(TransactionType.PURCHASE);

        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        Transaction result = transactionService.createTransaction(transaction);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(100), result.getAmount());
        verify(transactionRepository, times(1)).save(transaction);
    }

    @Test
    void getById_shouldReturnTransactionWhenExists() {
        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setAmount(BigDecimal.valueOf(100));

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        Transaction result = transactionService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getById_shouldThrowExceptionWhenNotFound() {
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            transactionService.getById(99L);
        });

        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void getTransactionContext_shouldReturnContextDto() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setFirstName("John");
        customer.setCountry("US");

        Account account = new Account();
        account.setId(1L);
        account.setAccountNumber("ACC-001");
        account.setAccountType(AccountType.CHECKING);
        account.setCustomer(customer);

        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setAmount(BigDecimal.valueOf(150.50));
        transaction.setTransactionType(TransactionType.PURCHASE);
        transaction.setMerchantName("Test Store");
        transaction.setMerchantCategory(MerchantCategory.GROCERY);
        transaction.setCountry("US");
        transaction.setCity("New York");
        transaction.setTransactionTimestamp(LocalDateTime.now());
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setAccount(account);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        TransactionContextDto context = transactionService.getTransactionContext(1L);

        assertNotNull(context);
        assertNotNull(context.getTransaction());
        assertNotNull(context.getCustomer());
        assertNotNull(context.getAccount());
        assertEquals(1L, context.getTransaction().getId());
        assertEquals("John", context.getCustomer().getFirstName());
        assertEquals("ACC-001", context.getAccount().getAccountNumber());
    }
}