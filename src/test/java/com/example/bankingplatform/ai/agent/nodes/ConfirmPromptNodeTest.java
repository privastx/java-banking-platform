package com.example.bankingplatform.ai.agent.nodes;

import com.example.bankingplatform.ai.agent.AppContext;
import com.example.bankingplatform.ai.agent.GraphRoutes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ConfirmPromptNodeTest {

    private final ConfirmPromptNode confirmPromptNode = new ConfirmPromptNode();

    @Test
    void apply_sets_awaiting_confirmation_state() {
        AppContext state = AppContext.builder().build();

        Map<String, Object> result = confirmPromptNode.apply(state);

        assertTrue((Boolean) result.get(GraphRoutes.CONFIRMATION_PENDING));
        assertTrue((Boolean) result.get(GraphRoutes.AWAITING_CONFIRMATION));
    }

    @Test
    void apply_returns_confirmation_prompt_message() {
        AppContext state = AppContext.builder().build();

        Map<String, Object> result = confirmPromptNode.apply(state);

        assertEquals("Before I can create a support ticket for your lost or stolen card, please confirm by typing CONFIRM or CANCEL.", result.get(GraphRoutes.AGENT_REPLY));
    }

    @Test
    void apply_does_not_change_confirmation_result() {
        AppContext state = AppContext.builder().build();

        Map<String, Object> result = confirmPromptNode.apply(state);

        assertFalse(result.containsKey(GraphRoutes.CONFIRMATION_RESULT));
    }

    @Test
    void apply_overwrites_existing_awaiting_confirmation_false() {
        AppContext state = AppContext.builder()
                .awaitingConfirmation(false)
                .confirmationPending(false)
                .build();

        Map<String, Object> result = confirmPromptNode.apply(state);

        assertTrue((Boolean) result.get(GraphRoutes.CONFIRMATION_PENDING));
        assertTrue((Boolean) result.get(GraphRoutes.AWAITING_CONFIRMATION));
    }

    @Test
    void apply_overwrites_existing_awaiting_confirmation_true() {
        AppContext state = AppContext.builder()
                .awaitingConfirmation(true)
                .confirmationPending(true)
                .build();

        Map<String, Object> result = confirmPromptNode.apply(state);

        assertTrue((Boolean) result.get(GraphRoutes.CONFIRMATION_PENDING));
        assertTrue((Boolean) result.get(GraphRoutes.AWAITING_CONFIRMATION));
    }

    @Test
    void apply_preserves_other_state_fields() {
        AppContext state = AppContext.builder()
                .sessionId("session-123")
                .userMessage("John Smith")
                .awaitingIdentity(false)
                .build();

        Map<String, Object> result = confirmPromptNode.apply(state);

        assertTrue((Boolean) result.get(GraphRoutes.CONFIRMATION_PENDING));
        assertTrue((Boolean) result.get(GraphRoutes.AWAITING_CONFIRMATION));
    }
}