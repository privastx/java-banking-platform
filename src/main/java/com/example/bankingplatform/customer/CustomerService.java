package com.example.bankingplatform.customer;

import com.example.bankingplatform.account.AccountDto;
import com.example.bankingplatform.transaction.TransactionDto;
import com.example.bankingplatform.support.SupportTicketDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional
    public Customer createCustomer(Customer customer) {
        log.info("Creating new customer: {}", customer.getEmail());
        return customerRepository.save(customer);
    }

    @Transactional(readOnly = true)
    public Customer getById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));
    }

    @Transactional
    public Customer updateCustomer(Long id, Customer updatedCustomer) {
        Customer customer = getById(id);
        customer.setFirstName(updatedCustomer.getFirstName());
        customer.setLastName(updatedCustomer.getLastName());
        customer.setEmail(updatedCustomer.getEmail());
        customer.setPhone(updatedCustomer.getPhone());
        customer.setDateOfBirth(updatedCustomer.getDateOfBirth());
        customer.setCountry(updatedCustomer.getCountry());
        customer.setStatus(updatedCustomer.getStatus());
        log.info("Updated customer: {}", id);
        return customerRepository.save(customer);
    }

    @Transactional
    public void deleteCustomer(Long id) {
        Customer customer = getById(id);
        customerRepository.delete(customer);
        log.info("Deleted customer: {}", id);
    }

    @Transactional(readOnly = true)
    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<AccountDto> getCustomerAccounts(Long customerId) {
        getById(customerId);
        return customerRepository.findById(customerId)
                .map(customer -> customer.getAccounts().stream()
                        .map(account -> new AccountDto(
                                account.getId(),
                                customer.getId(),
                                account.getAccountNumber(),
                                account.getAccountType(),
                                account.getCurrency(),
                                account.getBalance(),
                                account.getStatus(),
                                account.getCreatedAt(),
                                account.getUpdatedAt()))
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }

    @Transactional(readOnly = true)
    public List<TransactionDto> getCustomerTransactions(Long customerId) {
        getById(customerId);
        return customerRepository.findById(customerId)
                .map(customer -> customer.getAccounts().stream()
                        .flatMap(account -> account.getTransactions().stream())
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

    @Transactional(readOnly = true)
    public List<SupportTicketDto> getCustomerSupportTickets(Long customerId) {
        getById(customerId);
        return customerRepository.findById(customerId)
                .map(customer -> customer.getSupportTickets().stream()
                        .map(ticket -> new SupportTicketDto(
                                ticket.getId(),
                                customer.getId(),
                                ticket.getSubject(),
                                ticket.getDescription(),
                                ticket.getCategory(),
                                ticket.getPriority(),
                                ticket.getStatus(),
                                ticket.getCreatedAt(),
                                ticket.getUpdatedAt(),
                                ticket.getResolvedAt()))
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }
}