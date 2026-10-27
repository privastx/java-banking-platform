package com.example.bankingplatform.ai.agent.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WorkflowTest {

    @Test
    void fromValue_transaction_history_uppercase() {
        assertEquals(Workflow.TRANSACTION_HISTORY, Workflow.fromValue("TRANSACTION_HISTORY"));
    }

    @Test
    void fromValue_transaction_history_lowercase() {
        assertEquals(Workflow.TRANSACTION_HISTORY, Workflow.fromValue("transaction_history"));
    }

    @Test
    void fromValue_transaction_history_mixedcase() {
        assertEquals(Workflow.TRANSACTION_HISTORY, Workflow.fromValue("Transaction_History"));
    }

    @Test
    void fromValue_lost_or_stolen_card_uppercase() {
        assertEquals(Workflow.LOST_OR_STOLEN_CARD, Workflow.fromValue("LOST_OR_STOLEN_CARD"));
    }

    @Test
    void fromValue_lost_or_stolen_card_lowercase() {
        assertEquals(Workflow.LOST_OR_STOLEN_CARD, Workflow.fromValue("lost_or_stolen_card"));
    }

    @Test
    void fromValue_support_tickets_uppercase() {
        assertEquals(Workflow.SUPPORT_TICKETS, Workflow.fromValue("SUPPORT_TICKETS"));
    }

    @Test
    void fromValue_support_tickets_lowercase() {
        assertEquals(Workflow.SUPPORT_TICKETS, Workflow.fromValue("support_tickets"));
    }

    @Test
    void fromValue_null_throws_exception() {
        assertThrows(IllegalArgumentException.class, () -> Workflow.fromValue(null));
    }

    @Test
    void fromValue_unknown_throws_exception() {
        assertThrows(IllegalArgumentException.class, () -> Workflow.fromValue("UNKNOWN_WORKFLOW"));
    }

    @Test
    void fromValue_empty_throws_exception() {
        assertThrows(IllegalArgumentException.class, () -> Workflow.fromValue(""));
    }

    @Test
    void values_contains_all_workflows() {
        Workflow[] values = Workflow.values();
        assertEquals(3, values.length);
        assertTrue(java.util.List.of(values).contains(Workflow.TRANSACTION_HISTORY));
        assertTrue(java.util.List.of(values).contains(Workflow.LOST_OR_STOLEN_CARD));
        assertTrue(java.util.List.of(values).contains(Workflow.SUPPORT_TICKETS));
    }

    @Test
    void name_returns_enum_name() {
        assertEquals("TRANSACTION_HISTORY", Workflow.TRANSACTION_HISTORY.name());
        assertEquals("LOST_OR_STOLEN_CARD", Workflow.LOST_OR_STOLEN_CARD.name());
        assertEquals("SUPPORT_TICKETS", Workflow.SUPPORT_TICKETS.name());
    }
}