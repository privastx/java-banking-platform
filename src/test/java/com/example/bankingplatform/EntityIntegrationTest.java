package com.example.bankingplatform;

import com.example.bankingplatform.customer.Customer;
import com.example.bankingplatform.customer.CustomerStatus;
import com.example.bankingplatform.account.Account;
import com.example.bankingplatform.account.AccountType;
import com.example.bankingplatform.transaction.Transaction;
import com.example.bankingplatform.transaction.TransactionType;
import com.example.bankingplatform.support.SupportTicket;
import com.example.bankingplatform.support.SupportCategory;
import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
class EntityIntegrationTest {

    static {
        Dotenv dotenv = Dotenv.configure()
            .directory(".")
            .filename(".env.test")
            .ignoreIfMissing()
            .load();
        
        dotenv.entries().forEach(entry ->
            System.setProperty(entry.getKey(), entry.getValue())
        );
    }

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldCreateAndFindCustomer() {
        Customer customer = new Customer();
        customer.setFirstName("Test");
        customer.setLastName("User");
        customer.setEmail("test@email.com");
        customer.setCountry("US");
        customer.setStatus(CustomerStatus.ACTIVE);

        entityManager.persistAndFlush(customer);

        Customer found = entityManager.find(Customer.class, customer.getId());

        assertNotNull(found);
        assertEquals("Test", found.getFirstName());
        assertEquals("test@email.com", found.getEmail());
    }

    @Test
    void shouldCreateAndFindAccount() {
        Customer customer = new Customer();
        customer.setFirstName("Test");
        customer.setLastName("User");
        customer.setEmail("account@test.com");
        customer.setCountry("US");

        Account account = new Account();
        account.setCustomer(customer);
        account.setAccountNumber("ACC-INT-001");
        account.setAccountType(AccountType.CHECKING);
        account.setBalance(BigDecimal.valueOf(1000));

        entityManager.persist(customer);
        entityManager.persistAndFlush(account);

        Account found = entityManager.find(Account.class, account.getId());

        assertNotNull(found);
        assertEquals("ACC-INT-001", found.getAccountNumber());
        assertEquals(BigDecimal.valueOf(1000), found.getBalance());
    }

    @Test
    void shouldCreateAndFindTransaction() {
        Customer customer = new Customer();
        customer.setFirstName("Test");
        customer.setLastName("User");
        customer.setEmail("trans@test.com");
        customer.setCountry("US");

        Account account = new Account();
        account.setCustomer(customer);
        account.setAccountNumber("ACC-TRANS-001");
        account.setAccountType(AccountType.CHECKING);
        account.setBalance(BigDecimal.valueOf(5000));

        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setTransactionType(TransactionType.PURCHASE);
        transaction.setAmount(BigDecimal.valueOf(100));
        transaction.setMerchantName("Test Merchant");

        entityManager.persist(customer);
        entityManager.persist(account);
        entityManager.persistAndFlush(transaction);

        Transaction found = entityManager.find(Transaction.class, transaction.getId());

        assertNotNull(found);
        assertEquals(BigDecimal.valueOf(100), found.getAmount());
        assertEquals("Test Merchant", found.getMerchantName());
    }

    @Test
    void shouldCreateAndFindSupportTicket() {
        Customer customer = new Customer();
        customer.setFirstName("Test");
        customer.setLastName("User");
        customer.setEmail("ticket@test.com");
        customer.setCountry("US");

        SupportTicket ticket = new SupportTicket();
        ticket.setCustomer(customer);
        ticket.setSubject("Test Subject");
        ticket.setDescription("Test Description");
        ticket.setCategory(SupportCategory.ACCOUNT);

        entityManager.persist(customer);
        entityManager.persistAndFlush(ticket);

        SupportTicket found = entityManager.find(SupportTicket.class, ticket.getId());

        assertNotNull(found);
        assertEquals("Test Subject", found.getSubject());
        assertEquals(SupportCategory.ACCOUNT, found.getCategory());
    }
}