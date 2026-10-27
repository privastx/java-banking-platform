package com.example.bankingplatform.ai.agent;

import com.example.bankingplatform.ai.agent.models.CustomerIdentity;
import com.example.bankingplatform.ai.agent.models.Workflow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AppContextTest {

    private AppContext.Builder builder;

    @BeforeEach
    void setUp() {
        builder = AppContext.builder();
    }

    @Test
    void builder_build_empty() {
        AppContext context = builder.build();
        assertNull(context.sessionId());
        assertNull(context.workflow());
        assertNull(context.userMessage());
        assertNull(context.confirmationAction());
        assertNull(context.customerIdentity());
        assertFalse(context.awaitingIdentity());
        assertFalse(context.awaitingConfirmation());
        assertFalse(context.confirmationPending());
        assertNull(context.confirmationResult());
        assertNull(context.agentReply());
        assertFalse(context.isIdentityResolved());
        assertFalse(context.isComplete());
    }

    @Test
    void builder_build_with_all_fields() {
        AppContext context = builder
                .sessionId("session-123")
                .workflow(Workflow.TRANSACTION_HISTORY)
                .userMessage("John Smith")
                .confirmationAction("CONFIRM")
                .customerIdentity(new CustomerIdentity(1L, "John", "Smith"))
                .awaitingIdentity(false)
                .awaitingConfirmation(false)
                .confirmationPending(false)
                .confirmationResult("CONFIRM")
                .agentReply("Here are your transactions.")
                .build();

        assertEquals("session-123", context.sessionId());
        assertEquals(Workflow.TRANSACTION_HISTORY, context.workflow());
        assertEquals("John Smith", context.userMessage());
        assertEquals("CONFIRM", context.confirmationAction());
        assertNotNull(context.customerIdentity());
        assertEquals(1L, context.customerIdentity().id());
        assertFalse(context.awaitingIdentity());
        assertFalse(context.awaitingConfirmation());
        assertFalse(context.confirmationPending());
        assertEquals("CONFIRM", context.confirmationResult());
        assertEquals("Here are your transactions.", context.agentReply());
        assertTrue(context.isIdentityResolved());
        assertTrue(context.isComplete());
    }

    @Test
    void builder_build_with_null_values_ignored() {
        AppContext context = builder
                .sessionId(null)
                .workflow(null)
                .userMessage(null)
                .build();

        assertNull(context.sessionId());
        assertNull(context.workflow());
        assertNull(context.userMessage());
    }

    @Test
    void builder_build_with_partial_fields() {
        AppContext context = builder
                .sessionId("session-456")
                .awaitingIdentity(true)
                .build();

        assertEquals("session-456", context.sessionId());
        assertTrue(context.awaitingIdentity());
        assertFalse(context.awaitingConfirmation());
        assertNull(context.workflow());
    }

    @Test
    void withSessionId_returns_new_context() {
        AppContext original = AppContext.builder().sessionId("old-session").build();
        AppContext updated = original.withSessionId("new-session");

        assertEquals("old-session", original.sessionId());
        assertEquals("new-session", updated.sessionId());
    }

    @Test
    void withWorkflow_returns_new_context() {
        AppContext original = AppContext.builder().build();
        AppContext updated = original.withWorkflow(Workflow.LOST_OR_STOLEN_CARD);

        assertNull(original.workflow());
        assertEquals(Workflow.LOST_OR_STOLEN_CARD, updated.workflow());
    }

    @Test
    void withAwaitingIdentity_returns_new_context() {
        AppContext original = AppContext.builder().awaitingIdentity(false).build();
        AppContext updated = original.withAwaitingIdentity(true);

        assertFalse(original.awaitingIdentity());
        assertTrue(updated.awaitingIdentity());
    }

    @Test
    void withAwaitingConfirmation_returns_new_context() {
        AppContext original = AppContext.builder().awaitingConfirmation(false).build();
        AppContext updated = original.withAwaitingConfirmation(true);

        assertFalse(original.awaitingConfirmation());
        assertTrue(updated.awaitingConfirmation());
    }

    @Test
    void withConfirmationResult_returns_new_context() {
        AppContext original = AppContext.builder().build();
        AppContext updated = original.withConfirmationResult("CANCEL");

        assertNull(original.confirmationResult());
        assertEquals("CANCEL", updated.confirmationResult());
    }

    @Test
    void withAgentReply_returns_new_context() {
        AppContext original = AppContext.builder().build();
        AppContext updated = original.withAgentReply("Done.");

        assertNull(original.agentReply());
        assertEquals("Done.", updated.agentReply());
    }

    @Test
    void withCustomerIdentity_returns_new_context() {
        AppContext original = AppContext.builder().build();
        CustomerIdentity identity = new CustomerIdentity(2L, "Jane", "Doe");
        AppContext updated = original.withCustomerIdentity(identity);

        assertNull(original.customerIdentity());
        assertEquals(identity, updated.customerIdentity());
        assertTrue(updated.isIdentityResolved());
    }

    @Test
    void merge_preserves_incoming_values() {
        AppContext original = AppContext.builder()
                .sessionId("session-1")
                .awaitingIdentity(false)
                .awaitingConfirmation(false)
                .confirmationPending(false)
                .confirmationResult("CONFIRM")
                .agentReply("Original reply")
                .build();

        AppContext incoming = AppContext.builder()
                .awaitingIdentity(true)
                .awaitingConfirmation(true)
                .confirmationPending(true)
                .confirmationResult("CANCEL")
                .agentReply("Merged reply")
                .build();

        AppContext merged = original.merge(incoming);

        assertEquals("session-1", merged.sessionId());
        assertTrue(merged.awaitingIdentity());
        assertTrue(merged.awaitingConfirmation());
        assertTrue(merged.confirmationPending());
        assertEquals("CANCEL", merged.confirmationResult());
        assertEquals("Merged reply", merged.agentReply());
    }

    @Test
    void merge_preserves_original_when_incoming_is_empty() {
        AppContext original = AppContext.builder()
                .sessionId("session-2")
                .awaitingIdentity(true)
                .agentReply("Keep this")
                .build();

        AppContext incoming = AppContext.builder().build();

        AppContext merged = original.merge(incoming);

        assertEquals("session-2", merged.sessionId());
        assertTrue(merged.awaitingIdentity());
        assertEquals("Keep this", merged.agentReply());
    }

    @Test
    void merge_preserves_original_null_values_when_incoming_not_set() {
        AppContext original = AppContext.builder()
                .sessionId("session-3")
                .customerIdentity(new CustomerIdentity(3L, "Bob", "Jones"))
                .awaitingIdentity(false)
                .awaitingConfirmation(false)
                .confirmationPending(false)
                .confirmationResult(null)
                .agentReply(null)
                .build();

        AppContext incoming = AppContext.builder().build();

        AppContext merged = original.merge(incoming);

        assertEquals("session-3", merged.sessionId());
        assertNotNull(merged.customerIdentity());
        assertEquals(3L, merged.customerIdentity().id());
        assertFalse(merged.awaitingIdentity());
        assertFalse(merged.awaitingConfirmation());
        assertFalse(merged.confirmationPending());
        assertNull(merged.confirmationResult());
        assertNull(merged.agentReply());
    }

    @Test
    void isIdentityResolved_returns_false_when_no_identity() {
        AppContext context = AppContext.builder().build();
        assertFalse(context.isIdentityResolved());
    }

    @Test
    void isIdentityResolved_returns_true_when_identity_set() {
        AppContext context = AppContext.builder()
                .customerIdentity(new CustomerIdentity(1L, "Alice", "Smith"))
                .build();
        assertTrue(context.isIdentityResolved());
    }

    @Test
    void isComplete_returns_false_when_awaiting_identity() {
        AppContext context = AppContext.builder()
                .awaitingIdentity(true)
                .agentReply("Done")
                .build();
        assertFalse(context.isComplete());
    }

    @Test
    void isComplete_returns_false_when_awaiting_confirmation() {
        AppContext context = AppContext.builder()
                .awaitingConfirmation(true)
                .agentReply("Done")
                .build();
        assertFalse(context.isComplete());
    }

    @Test
    void isComplete_returns_true_when_all_set() {
        AppContext context = AppContext.builder()
                .awaitingIdentity(false)
                .awaitingConfirmation(false)
                .agentReply("Done")
                .build();
        assertTrue(context.isComplete());
    }

    @Test
    void isComplete_returns_false_when_no_agent_reply() {
        AppContext context = AppContext.builder()
                .awaitingIdentity(false)
                .awaitingConfirmation(false)
                .build();
        assertFalse(context.isComplete());
    }

    @Test
    void constructor_with_data_map() {
        Map<String, Object> data = Map.of(
                "sessionId", "session-4",
                "userMessage", "Hello",
                "awaitingIdentity", true
        );

        AppContext context = new AppContext(data);

        assertEquals("session-4", context.sessionId());
        assertEquals("Hello", context.userMessage());
        assertTrue(context.awaitingIdentity());
    }

    @Test
    void data_returns_underlying_map() {
        AppContext context = AppContext.builder()
                .sessionId("session-5")
                .workflow(Workflow.SUPPORT_TICKETS)
                .build();

        Map<String, Object> data = context.data();
        assertNotNull(data);
        assertTrue(data.containsKey("sessionId"));
        assertEquals("session-5", data.get("sessionId"));
    }

    @Test
    void value_returns_present_value() {
        AppContext context = AppContext.builder()
                .sessionId("session-6")
                .build();

        assertTrue(context.value("sessionId").isPresent());
        assertEquals("session-6", context.value("sessionId").orElse(null));
    }

    @Test
    void value_returns_empty_for_missing_key() {
        AppContext context = AppContext.builder().build();

        assertFalse(context.value("nonexistent").isPresent());
    }
}