package com.example.bankingplatform.ai.agent.nodes;

import com.example.bankingplatform.ai.agent.AppContext;
import com.example.bankingplatform.ai.agent.GraphRoutes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ConfirmNodeTest {

    private final ConfirmNode confirmNode = new ConfirmNode();

    @Test
    void apply_confirms_with_uppercase() {
        AppContext state = AppContext.builder()
                .userMessage("CONFIRM")
                .awaitingConfirmation(true)
                .confirmationPending(true)
                .build();

        Map<String, Object> result = confirmNode.apply(state);

        assertEquals("CONFIRM", result.get(GraphRoutes.CONFIRMATION_RESULT));
        assertFalse((Boolean) result.get(GraphRoutes.CONFIRMATION_PENDING));
        assertFalse((Boolean) result.get(GraphRoutes.AWAITING_CONFIRMATION));
        assertEquals("Proceeding with your lost/stolen card report...", result.get(GraphRoutes.AGENT_REPLY));
    }

    @Test
    void apply_confirms_with_lowercase() {
        AppContext state = AppContext.builder()
                .userMessage("confirm")
                .awaitingConfirmation(true)
                .confirmationPending(true)
                .build();

        Map<String, Object> result = confirmNode.apply(state);

        assertEquals("CONFIRM", result.get(GraphRoutes.CONFIRMATION_RESULT));
        assertFalse((Boolean) result.get(GraphRoutes.CONFIRMATION_PENDING));
        assertFalse((Boolean) result.get(GraphRoutes.AWAITING_CONFIRMATION));
    }

    @Test
    void apply_confirms_with_mixedcase() {
        AppContext state = AppContext.builder()
                .userMessage("Confirm")
                .awaitingConfirmation(true)
                .confirmationPending(true)
                .build();

        Map<String, Object> result = confirmNode.apply(state);

        assertEquals("CONFIRM", result.get(GraphRoutes.CONFIRMATION_RESULT));
    }

    @Test
    void apply_confirms_with_all_caps_with_spaces() {
        AppContext state = AppContext.builder()
                .userMessage("  CONFIRM  ")
                .awaitingConfirmation(true)
                .confirmationPending(true)
                .build();

        Map<String, Object> result = confirmNode.apply(state);

        assertEquals("CONFIRM", result.get(GraphRoutes.CONFIRMATION_RESULT));
        assertFalse((Boolean) result.get(GraphRoutes.CONFIRMATION_PENDING));
    }

    @Test
    void apply_cancels_with_uppercase() {
        AppContext state = AppContext.builder()
                .userMessage("CANCEL")
                .awaitingConfirmation(true)
                .confirmationPending(true)
                .build();

        Map<String, Object> result = confirmNode.apply(state);

        assertEquals("CANCEL", result.get(GraphRoutes.CONFIRMATION_RESULT));
        assertFalse((Boolean) result.get(GraphRoutes.CONFIRMATION_PENDING));
        assertFalse((Boolean) result.get(GraphRoutes.AWAITING_CONFIRMATION));
        assertEquals("Your lost/stolen card report has been cancelled.", result.get(GraphRoutes.AGENT_REPLY));
    }

    @Test
    void apply_cancels_with_lowercase() {
        AppContext state = AppContext.builder()
                .userMessage("cancel")
                .awaitingConfirmation(true)
                .confirmationPending(true)
                .build();

        Map<String, Object> result = confirmNode.apply(state);

        assertEquals("CANCEL", result.get(GraphRoutes.CONFIRMATION_RESULT));
    }

    @Test
    void apply_cancels_with_mixedcase() {
        AppContext state = AppContext.builder()
                .userMessage("Cancel")
                .awaitingConfirmation(true)
                .confirmationPending(true)
                .build();

        Map<String, Object> result = confirmNode.apply(state);

        assertEquals("CANCEL", result.get(GraphRoutes.CONFIRMATION_RESULT));
    }

    @Test
    void apply_invalid_input_reprompts() {
        AppContext state = AppContext.builder()
                .userMessage("yes")
                .awaitingConfirmation(true)
                .confirmationPending(true)
                .build();

        Map<String, Object> result = confirmNode.apply(state);

        assertEquals("Please respond with CONFIRM or CANCEL to proceed with your lost/stolen card report.", result.get(GraphRoutes.AGENT_REPLY));
    }

    @Test
    void apply_invalid_input_reprompts_with_number() {
        AppContext state = AppContext.builder()
                .userMessage("123")
                .awaitingConfirmation(true)
                .confirmationPending(true)
                .build();

        Map<String, Object> result = confirmNode.apply(state);

        assertEquals("Please respond with CONFIRM or CANCEL to proceed with your lost/stolen card report.", result.get(GraphRoutes.AGENT_REPLY));
    }

    @Test
    void apply_empty_message_reprompts() {
        AppContext state = AppContext.builder()
                .userMessage("")
                .awaitingConfirmation(true)
                .confirmationPending(true)
                .build();

        Map<String, Object> result = confirmNode.apply(state);

        assertEquals("Please respond with CONFIRM or CANCEL to proceed with your lost/stolen card report.", result.get(GraphRoutes.AGENT_REPLY));
    }

    @Test
    void apply_null_message_reprompts() {
        AppContext state = AppContext.builder()
                .userMessage(null)
                .awaitingConfirmation(true)
                .confirmationPending(true)
                .build();

        Map<String, Object> result = confirmNode.apply(state);

        assertEquals("Please respond with CONFIRM or CANCEL to proceed with your lost/stolen card report.", result.get(GraphRoutes.AGENT_REPLY));
    }

    @Test
    void apply_whitespace_only_reprompts() {
        AppContext state = AppContext.builder()
                .userMessage("   ")
                .awaitingConfirmation(true)
                .confirmationPending(true)
                .build();

        Map<String, Object> result = confirmNode.apply(state);

        assertEquals("Please respond with CONFIRM or CANCEL to proceed with your lost/stolen card report.", result.get(GraphRoutes.AGENT_REPLY));
    }

    @Test
    void apply_confirms_without_awaiting_confirmation_flag() {
        AppContext state = AppContext.builder()
                .userMessage("CONFIRM")
                .awaitingConfirmation(false)
                .confirmationPending(false)
                .build();

        Map<String, Object> result = confirmNode.apply(state);

        assertEquals("CONFIRM", result.get(GraphRoutes.CONFIRMATION_RESULT));
        assertFalse((Boolean) result.get(GraphRoutes.CONFIRMATION_PENDING));
        assertFalse((Boolean) result.get(GraphRoutes.AWAITING_CONFIRMATION));
    }
}