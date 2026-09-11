package com.example.bankingplatform.support;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

    List<SupportTicket> findByCustomerId(Long customerId);

    List<SupportTicket> findByStatus(SupportStatus status);
}