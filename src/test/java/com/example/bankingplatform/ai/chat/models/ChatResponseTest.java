package com.example.bankingplatform.ai.chat.models;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChatResponseTest {

    @Test
    void record_with_all_fields() {
        ChatResponse response = new ChatResponse(
                "session-123",
                "Here are your transactions.",
                "COMPLETED",
                "TRANSACTION_HISTORY",
                List.of()
        );

        assertEquals("session-123", response.sessionId());
        assertEquals("Here are your transactions.", response.message());
        assertEquals("COMPLETED", response.state());
        assertEquals("TRANSACTION_HISTORY", response.workflow());
        assertEquals(List.of(), response.availableActions());
    }

    @Test
    void record_with_available_actions() {
        ChatResponse response = new ChatResponse(
                "session-456",
                "Please confirm or cancel.",
                "AWAITING_CONFIRMATION",
                "LOST_OR_STOLEN_CARD",
                List.of("CONFIRM", "CANCEL")
        );

        assertEquals("AWAITING_CONFIRMATION", response.state());
        assertEquals(2, response.availableActions().size());
        assertTrue(response.availableActions().contains("CONFIRM"));
        assertTrue(response.availableActions().contains("CANCEL"));
    }

    @Test
    void record_with_null_workflow() {
        ChatResponse response = new ChatResponse(
                "session-789",
                "RAG response.",
                null,
                null,
                List.of()
        );

        assertNull(response.workflow());
        assertNull(response.state());
        assertEquals("RAG response.", response.message());
    }

    @Test
    void record_with_null_available_actions() {
        ChatResponse response = new ChatResponse(
                "session-1",
                "Message.",
                "COMPLETED",
                "SUPPORT_TICKETS",
                null
        );

        assertNull(response.availableActions());
    }

    @Test
    void record_equals_and_hashCode() {
        ChatResponse r1 = new ChatResponse("s1", "msg", "state", "wf", List.of());
        ChatResponse r2 = new ChatResponse("s1", "msg", "state", "wf", List.of());

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void record_not_equals_when_session_differs() {
        ChatResponse r1 = new ChatResponse("s1", "msg", null, null, List.of());
        ChatResponse r2 = new ChatResponse("s2", "msg", null, null, List.of());

        assertNotEquals(r1, r2);
    }

    @Test
    void record_not_equals_when_message_differs() {
        ChatResponse r1 = new ChatResponse("s1", "msg1", null, null, List.of());
        ChatResponse r2 = new ChatResponse("s1", "msg2", null, null, List.of());

        assertNotEquals(r1, r2);
    }

    @Test
    void record_toString_contains_fields() {
        ChatResponse response = new ChatResponse(
                "session-1",
                "Hello",
                "COMPLETED",
                "TRANSACTION_HISTORY",
                List.of()
        );

        String str = response.toString();
        assertTrue(str.contains("session-1"));
        assertTrue(str.contains("Hello"));
        assertTrue(str.contains("COMPLETED"));
    }

    @Test
    void record_with_empty_message() {
        ChatResponse response = new ChatResponse("s1", "", null, null, List.of());

        assertEquals("", response.message());
    }

    @Test
    void record_with_empty_list_available_actions() {
        ChatResponse response = new ChatResponse("s1", "msg", null, null, List.of());

        assertNotNull(response.availableActions());
        assertTrue(response.availableActions().isEmpty());
    }
}