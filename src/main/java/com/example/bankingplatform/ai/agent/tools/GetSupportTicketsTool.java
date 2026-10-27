package com.example.bankingplatform.ai.agent.tools;

import com.example.bankingplatform.support.SupportTicket;
import com.example.bankingplatform.support.SupportTicketRepository;
import dev.langchain4j.agent.tool.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GetSupportTicketsTool {

    private static final Logger log = LoggerFactory.getLogger(GetSupportTicketsTool.class);

    private final SupportTicketRepository supportTicketRepository;

    public GetSupportTicketsTool(SupportTicketRepository supportTicketRepository) {
        this.supportTicketRepository = supportTicketRepository;
    }

    @Tool("Get all support tickets for a customer")
    public List<TicketSummary> getSupportTickets(Long customerId) {
        log.info("Getting support tickets for customerId={}", customerId);

        if (customerId == null || customerId <= 0) {
            return List.of();
        }

        try {
            List<SupportTicket> tickets = supportTicketRepository.findByCustomerId(customerId);

            List<TicketSummary> result = tickets.stream()
                    .sorted(Comparator.comparing(SupportTicket::getCreatedAt).reversed())
                    .map(ticket -> new TicketSummary(
                            ticket.getId(),
                            ticket.getSubject(),
                            ticket.getCategory().name(),
                            ticket.getPriority().name(),
                            ticket.getStatus().name(),
                            ticket.getCreatedAt()))
                    .collect(Collectors.toList());

            return result;
        } catch (Exception e) {
            log.error("Failed to get support tickets for customerId={}", customerId, e);
            return List.of();
        }
    }

    public record TicketSummary(
            Long id,
            String subject,
            String category,
            String priority,
            String status,
            java.time.LocalDateTime createdAt
    ) {}
}