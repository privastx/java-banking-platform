package com.example.bankingplatform.ai.agent.tools;

import com.example.bankingplatform.customer.Customer;
import com.example.bankingplatform.support.SupportCategory;
import com.example.bankingplatform.support.SupportPriority;
import com.example.bankingplatform.support.SupportStatus;
import com.example.bankingplatform.support.SupportTicket;
import com.example.bankingplatform.support.SupportTicketService;
import dev.langchain4j.agent.tool.Tool;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CreateSupportTicketTool {

    private static final Logger log = LoggerFactory.getLogger(CreateSupportTicketTool.class);

    private final SupportTicketService supportTicketService;

    @PersistenceContext
    private EntityManager entityManager;

    public CreateSupportTicketTool(SupportTicketService supportTicketService) {
        this.supportTicketService = supportTicketService;
    }

    @Tool("Create a support ticket for a lost or stolen card")
    public TicketResult createLostCardTicket(Long customerId) {
        log.info("Creating lost/stolen card ticket for customerId={}", customerId);

        if (customerId == null || customerId <= 0) {
            throw new IllegalArgumentException("Invalid customerId");
        }

        try {
            Customer customer = entityManager.find(Customer.class, customerId);
            if (customer == null) {
                throw new IllegalArgumentException("Customer not found with id: " + customerId);
            }

            SupportTicket ticket = new SupportTicket();
            ticket.setCustomer(customer);
            ticket.setSubject("Lost/Stolen Card Report");
            ticket.setDescription("Customer reported lost or stolen card");
            ticket.setCategory(SupportCategory.SECURITY);
            ticket.setPriority(SupportPriority.HIGH);
            ticket.setStatus(SupportStatus.OPEN);

            ticket = supportTicketService.createTicket(ticket);

            log.info("Support ticket created: id={}, customerId={}", ticket.getId(), customerId);
            return new TicketResult(ticket.getId().toString(), ticket.getStatus().name());
        } catch (Exception e) {
            log.error("Failed to create support ticket for customerId={}", customerId, e);
            throw new RuntimeException("Failed to create support ticket", e);
        }
    }

    public record TicketResult(
            String ticketId,
            String status
    ) {}
}