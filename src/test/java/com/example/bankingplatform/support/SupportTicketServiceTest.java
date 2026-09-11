package com.example.bankingplatform.support;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupportTicketServiceTest {

    @Mock
    private SupportTicketRepository supportTicketRepository;

    @InjectMocks
    private SupportTicketService supportTicketService;

    @Test
    void createTicket_shouldSaveAndReturnTicket() {
        SupportTicket ticket = new SupportTicket();
        ticket.setSubject("Test Subject");
        ticket.setDescription("Test Description");
        ticket.setCategory(SupportCategory.ACCOUNT);

        when(supportTicketRepository.save(any(SupportTicket.class))).thenReturn(ticket);

        SupportTicket result = supportTicketService.createTicket(ticket);

        assertNotNull(result);
        assertEquals("Test Subject", result.getSubject());
        verify(supportTicketRepository, times(1)).save(ticket);
    }

    @Test
    void getById_shouldReturnTicketWhenExists() {
        SupportTicket ticket = new SupportTicket();
        ticket.setId(1L);
        ticket.setSubject("Test Subject");

        when(supportTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        SupportTicket result = supportTicketService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getById_shouldThrowExceptionWhenNotFound() {
        when(supportTicketRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            supportTicketService.getById(99L);
        });

        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void updateTicket_shouldUpdateAndReturnTicket() {
        SupportTicket existingTicket = new SupportTicket();
        existingTicket.setId(1L);
        existingTicket.setSubject("Old Subject");
        existingTicket.setStatus(SupportStatus.OPEN);

        SupportTicket updateRequest = new SupportTicket();
        updateRequest.setSubject("New Subject");
        updateRequest.setStatus(SupportStatus.IN_PROGRESS);

        when(supportTicketRepository.findById(1L)).thenReturn(Optional.of(existingTicket));
        when(supportTicketRepository.save(any(SupportTicket.class))).thenReturn(existingTicket);

        SupportTicket result = supportTicketService.updateTicket(1L, updateRequest);

        assertEquals("New Subject", result.getSubject());
        assertEquals(SupportStatus.IN_PROGRESS, result.getStatus());
    }
}