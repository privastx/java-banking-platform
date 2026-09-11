package com.example.bankingplatform.transaction;

import com.example.bankingplatform.customer.CustomerDto;
import com.example.bankingplatform.account.AccountDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionContextDto {
    private TransactionDto transaction;
    private CustomerDto customer;
    private AccountDto account;
}
