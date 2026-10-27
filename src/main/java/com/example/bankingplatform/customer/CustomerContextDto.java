package com.example.bankingplatform.customer;

import com.example.bankingplatform.account.AccountDto;
import com.example.bankingplatform.transaction.TransactionDto;
import com.example.bankingplatform.support.SupportTicketDto;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;

@Getter
@Setter
@AllArgsConstructor
public class CustomerContextDto {
    private CustomerDto customer;
    private List<AccountDto> accounts;
    private List<TransactionDto> recentTransactions;
    private List<SupportTicketDto> supportTickets;
}
