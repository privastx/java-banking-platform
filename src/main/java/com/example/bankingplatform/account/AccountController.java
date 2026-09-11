package com.example.bankingplatform.account;

import com.example.bankingplatform.transaction.TransactionDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/accounts")
@Tag(name = "Account API", description = "Account management endpoints")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get account by ID")
    public ResponseEntity<AccountDto> getAccountById(@PathVariable Long id) {
        Account account = accountService.getById(id);
        return ResponseEntity.ok(toAccountDto(account));
    }

    @PostMapping
    @Operation(summary = "Create new account")
    public ResponseEntity<AccountDto> createAccount(@Valid @RequestBody Account account) {
        Account created = accountService.createAccount(account);
        return ResponseEntity.ok(toAccountDto(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update account")
    public ResponseEntity<AccountDto> updateAccount(@PathVariable Long id, @Valid @RequestBody Account account) {
        Account updated = accountService.updateAccount(id, account);
        return ResponseEntity.ok(toAccountDto(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete account")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id) {
        accountService.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/transactions")
    @Operation(summary = "Get account transactions")
    public ResponseEntity<List<TransactionDto>> getAccountTransactions(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.getAccountTransactions(id));
    }

    private AccountDto toAccountDto(Account account) {
        return new AccountDto(
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
    }
}