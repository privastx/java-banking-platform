package com.example.bankingplatform.support;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/support/tickets")
@Tag(name = "Support Ticket API", description = "Support ticket management endpoints")
public class SupportTicketController {

    private final SupportTicketService supportTicketService;

    public SupportTicketController(SupportTicketService supportTicketService) {
        this.supportTicketService = supportTicketService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get support ticket by ID")
    public ResponseEntity<SupportTicketDto> getTicketById(@PathVariable Long id) {
        SupportTicket ticket = supportTicketService.getById(id);
        return ResponseEntity.ok(toTicketDto(ticket));
    }

    @PostMapping
    @Operation(summary = "Create new support ticket")
    public ResponseEntity<SupportTicketDto> createTicket(@Valid @RequestBody SupportTicket ticket) {
        SupportTicket created = supportTicketService.createTicket(ticket);
        return ResponseEntity.ok(toTicketDto(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update support ticket")
    public ResponseEntity<SupportTicketDto> updateTicket(@PathVariable Long id, @Valid @RequestBody SupportTicket ticket) {
        SupportTicket updated = supportTicketService.updateTicket(id, ticket);
        return ResponseEntity.ok(toTicketDto(updated));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get customer support tickets")
    public ResponseEntity<List<SupportTicketDto>> getCustomerTickets(@PathVariable Long customerId) {
        return ResponseEntity.ok(supportTicketService.findByCustomerId(customerId).stream()
                .map(this::toTicketDto)
                .collect(Collectors.toList()));
    }

    private SupportTicketDto toTicketDto(SupportTicket ticket) {
        return new SupportTicketDto(
                ticket.getId(),
                ticket.getCustomer().getId(),
                ticket.getSubject(),
                ticket.getDescription(),
                ticket.getCategory(),
                ticket.getPriority(),
                ticket.getStatus(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                ticket.getResolvedAt()
        );
    }
}