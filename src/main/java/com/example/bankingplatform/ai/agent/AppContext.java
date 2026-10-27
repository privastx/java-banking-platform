package com.example.bankingplatform.ai.agent;

import com.example.bankingplatform.ai.agent.models.CustomerIdentity;
import com.example.bankingplatform.ai.agent.models.Workflow;
import org.bsc.langgraph4j.state.AgentState;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class AppContext extends AgentState {

    public AppContext(Map<String, Object> data) {
        super(data);
    }

    public String sessionId() {
        return (String) value(GraphRoutes.SESSION_ID).orElse(null);
    }

    public Workflow workflow() {
        return (Workflow) value(GraphRoutes.WORKFLOW).orElse(null);
    }

    public String userMessage() {
        return (String) value(GraphRoutes.USER_MESSAGE).orElse(null);
    }

    public String confirmationAction() {
        return (String) value(GraphRoutes.CONFIRMATION_ACTION).orElse(null);
    }

    public CustomerIdentity customerIdentity() {
        return (CustomerIdentity) value(GraphRoutes.CUSTOMER_IDENTITY).orElse(null);
    }

    public boolean awaitingIdentity() {
        return Boolean.TRUE.equals(value(GraphRoutes.AWAITING_IDENTITY).orElse(false));
    }

    public boolean awaitingConfirmation() {
        return Boolean.TRUE.equals(value(GraphRoutes.AWAITING_CONFIRMATION).orElse(false));
    }

    public boolean confirmationPending() {
        return Boolean.TRUE.equals(value(GraphRoutes.CONFIRMATION_PENDING).orElse(false));
    }

    public String confirmationResult() {
        return (String) value(GraphRoutes.CONFIRMATION_RESULT).orElse(null);
    }

    public String agentReply() {
        return (String) value(GraphRoutes.AGENT_REPLY).orElse(null);
    }

    public AppContext withSessionId(String sessionId) {
        Map<String, Object> data = new HashMap<>(this.data());
        data.put(GraphRoutes.SESSION_ID, sessionId);
        return new AppContext(data);
    }

    public AppContext withWorkflow(Workflow workflow) {
        Map<String, Object> data = new HashMap<>(this.data());
        data.put(GraphRoutes.WORKFLOW, workflow);
        return new AppContext(data);
    }

    public AppContext withUserMessage(String userMessage) {
        Map<String, Object> data = new HashMap<>(this.data());
        data.put(GraphRoutes.USER_MESSAGE, userMessage);
        return new AppContext(data);
    }

    public AppContext withConfirmationAction(String confirmationAction) {
        Map<String, Object> data = new HashMap<>(this.data());
        data.put(GraphRoutes.CONFIRMATION_ACTION, confirmationAction);
        return new AppContext(data);
    }

    public AppContext withCustomerIdentity(CustomerIdentity customerIdentity) {
        Map<String, Object> data = new HashMap<>(this.data());
        data.put(GraphRoutes.CUSTOMER_IDENTITY, customerIdentity);
        return new AppContext(data);
    }

    public AppContext withAwaitingIdentity(boolean awaitingIdentity) {
        Map<String, Object> data = new HashMap<>(this.data());
        data.put(GraphRoutes.AWAITING_IDENTITY, awaitingIdentity);
        return new AppContext(data);
    }

    public AppContext withAwaitingConfirmation(boolean awaitingConfirmation) {
        Map<String, Object> data = new HashMap<>(this.data());
        data.put(GraphRoutes.AWAITING_CONFIRMATION, awaitingConfirmation);
        return new AppContext(data);
    }

    public AppContext withConfirmationPending(boolean confirmationPending) {
        Map<String, Object> data = new HashMap<>(this.data());
        data.put(GraphRoutes.CONFIRMATION_PENDING, confirmationPending);
        return new AppContext(data);
    }

    public AppContext withConfirmationResult(String confirmationResult) {
        Map<String, Object> data = new HashMap<>(this.data());
        data.put(GraphRoutes.CONFIRMATION_RESULT, confirmationResult);
        return new AppContext(data);
    }

    public AppContext withAgentReply(String agentReply) {
        Map<String, Object> data = new HashMap<>(this.data());
        data.put(GraphRoutes.AGENT_REPLY, agentReply);
        return new AppContext(data);
    }

    public boolean isIdentityResolved() {
        return customerIdentity() != null;
    }

    public boolean isComplete() {
        return agentReply() != null && !awaitingIdentity() && !awaitingConfirmation();
    }

    public AppContext merge(AppContext incoming) {
        Map<String, Object> data = new HashMap<>(this.data());
        incoming.value(GraphRoutes.AWAITING_IDENTITY).ifPresent(v -> data.put(GraphRoutes.AWAITING_IDENTITY, v));
        incoming.value(GraphRoutes.AWAITING_CONFIRMATION).ifPresent(v -> data.put(GraphRoutes.AWAITING_CONFIRMATION, v));
        incoming.value(GraphRoutes.CONFIRMATION_PENDING).ifPresent(v -> data.put(GraphRoutes.CONFIRMATION_PENDING, v));
        incoming.value(GraphRoutes.CONFIRMATION_RESULT).ifPresent(v -> data.put(GraphRoutes.CONFIRMATION_RESULT, v));
        incoming.value(GraphRoutes.CUSTOMER_IDENTITY).ifPresent(v -> data.put(GraphRoutes.CUSTOMER_IDENTITY, v));
        incoming.value(GraphRoutes.AGENT_REPLY).ifPresent(v -> data.put(GraphRoutes.AGENT_REPLY, v));
        return new AppContext(data);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String sessionId;
        private Workflow workflow;
        private String userMessage;
        private String confirmationAction;
        private CustomerIdentity customerIdentity;
        private Boolean awaitingIdentity;
        private Boolean awaitingConfirmation;
        private Boolean confirmationPending;
        private String confirmationResult;
        private String agentReply;

        private Builder() {}

        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder workflow(Workflow workflow) {
            this.workflow = workflow;
            return this;
        }

        public Builder userMessage(String userMessage) {
            this.userMessage = userMessage;
            return this;
        }

        public Builder confirmationAction(String confirmationAction) {
            this.confirmationAction = confirmationAction;
            return this;
        }

        public Builder customerIdentity(CustomerIdentity customerIdentity) {
            this.customerIdentity = customerIdentity;
            return this;
        }

        public Builder awaitingIdentity(boolean awaitingIdentity) {
            this.awaitingIdentity = awaitingIdentity;
            return this;
        }

        public Builder awaitingConfirmation(boolean awaitingConfirmation) {
            this.awaitingConfirmation = awaitingConfirmation;
            return this;
        }

        public Builder confirmationPending(boolean confirmationPending) {
            this.confirmationPending = confirmationPending;
            return this;
        }

        public Builder confirmationResult(String confirmationResult) {
            this.confirmationResult = confirmationResult;
            return this;
        }

        public Builder agentReply(String agentReply) {
            this.agentReply = agentReply;
            return this;
        }

        public AppContext build() {
            Map<String, Object> data = new HashMap<>();
            if (sessionId != null) data.put(GraphRoutes.SESSION_ID, sessionId);
            if (workflow != null) data.put(GraphRoutes.WORKFLOW, workflow);
            if (userMessage != null) data.put(GraphRoutes.USER_MESSAGE, userMessage);
            if (confirmationAction != null) data.put(GraphRoutes.CONFIRMATION_ACTION, confirmationAction);
            if (customerIdentity != null) data.put(GraphRoutes.CUSTOMER_IDENTITY, customerIdentity);
            if (awaitingIdentity != null) data.put(GraphRoutes.AWAITING_IDENTITY, awaitingIdentity);
            if (awaitingConfirmation != null) data.put(GraphRoutes.AWAITING_CONFIRMATION, awaitingConfirmation);
            if (confirmationPending != null) data.put(GraphRoutes.CONFIRMATION_PENDING, confirmationPending);
            if (confirmationResult != null) data.put(GraphRoutes.CONFIRMATION_RESULT, confirmationResult);
            if (agentReply != null) data.put(GraphRoutes.AGENT_REPLY, agentReply);
            return new AppContext(data);
        }
    }
}