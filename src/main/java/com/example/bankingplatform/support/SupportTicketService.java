package com.example.bankingplatform.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SupportTicketService {

    private static final Logger log = LoggerFactory.getLogger(SupportTicketService.class);

    private final SupportTicketRepository supportTicketRepository;

    public SupportTicketService(SupportTicketRepository supportTicketRepository) {
        this.supportTicketRepository = supportTicketRepository;
    }

    @Transactional
    public SupportTicket createTicket(SupportTicket ticket) {
        log.info("Creating new support ticket: {}", ticket.getSubject());
        return supportTicketRepository.save(ticket);
    }

    @Transactional(readOnly = true)
    public SupportTicket getById(Long id) {
        return supportTicketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Support ticket not found with id: " + id));
    }

    @Transactional
    public SupportTicket updateTicket(Long id, SupportTicket updatedTicket) {
        SupportTicket ticket = getById(id);
        ticket.setSubject(updatedTicket.getSubject());
        ticket.setDescription(updatedTicket.getDescription());
        ticket.setCategory(updatedTicket.getCategory());
        ticket.setPriority(updatedTicket.getPriority());
        ticket.setStatus(updatedTicket.getStatus());
        log.info("Updated support ticket: {}", id);
        return supportTicketRepository.save(ticket);
    }

    @Transactional(readOnly = true)
    public List<SupportTicket> findByCustomerId(Long customerId) {
        return supportTicketRepository.findByCustomerId(customerId);
    }

    @Transactional(readOnly = true)
    public List<SupportTicket> findByStatus(SupportStatus status) {
        return supportTicketRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<SupportTicket> findAll() {
        return supportTicketRepository.findAll();
    }
}