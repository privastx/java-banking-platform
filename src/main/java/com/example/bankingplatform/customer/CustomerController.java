package com.example.bankingplatform.customer;

import com.example.bankingplatform.account.AccountDto;
import com.example.bankingplatform.transaction.TransactionDto;
import com.example.bankingplatform.support.SupportTicketDto;
import com.example.bankingplatform.customer.CustomerContextDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customers")
@Tag(name = "Customer API", description = "Customer management endpoints")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    @Operation(summary = "Get all customers")
    public ResponseEntity<List<CustomerDto>> getAllCustomers() {
        List<CustomerDto> customers = customerService.findAll().stream()
                .map(this::toCustomerDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID")
    public ResponseEntity<CustomerDto> getCustomerById(@PathVariable Long id) {
        Customer customer = customerService.getById(id);
        return ResponseEntity.ok(toCustomerDto(customer));
    }

    @PostMapping
    @Operation(summary = "Create new customer")
    public ResponseEntity<CustomerDto> createCustomer(@Valid @RequestBody Customer customer) {
        Customer created = customerService.createCustomer(customer);
        return ResponseEntity.ok(toCustomerDto(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update customer")
    public ResponseEntity<CustomerDto> updateCustomer(@PathVariable Long id,
 @Valid @RequestBody Customer customer) {
        Customer updated = customerService.updateCustomer(id, customer);
        return ResponseEntity.ok(toCustomerDto(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete customer")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/accounts")
    @Operation(summary = "Get customer accounts")
    public ResponseEntity<List<AccountDto>> getCustomerAccounts(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomerAccounts(id));
    }

    @GetMapping("/{id}/transactions")
    @Operation(summary = "Get customer transactions")
    public ResponseEntity<List<TransactionDto>> getCustomerTransactions(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomerTransactions(id));
    }

    @GetMapping("/{id}/support-tickets")
    @Operation(summary = "Get customer support tickets")
    public ResponseEntity<List<SupportTicketDto>> getCustomerSupportTickets(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomerSupportTickets(id));
    }

    @GetMapping("/{id}/context")
    @Operation(summary = "Get customer context for AI/RAG services")
    @Transactional(readOnly = true)
    public ResponseEntity<CustomerContextDto> getCustomerContext(@PathVariable Long id) {
        Customer customer = customerService.getById(id);
        List<AccountDto> accounts = customerService.getCustomerAccounts(id);
        List<TransactionDto> transactions = customerService.getCustomerTransactions(id).stream()
                .limit(10)
                .collect(Collectors.toList());
        List<SupportTicketDto> tickets = customerService.getCustomerSupportTickets(id);

        CustomerContextDto context = new CustomerContextDto(
                toCustomerDto(customer),
                accounts,
                transactions,
                tickets
        );
        return ResponseEntity.ok(context);
    }

    private CustomerDto toCustomerDto(Customer customer) {
        return new CustomerDto(
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
    }
}