package com.example.bankingplatform.ai.agent.nodes;

import com.example.bankingplatform.ai.agent.AppContext;
import com.example.bankingplatform.ai.agent.GraphRoutes;
import org.bsc.langgraph4j.action.NodeAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ConfirmNode implements NodeAction<AppContext> {

    private static final Logger log = LoggerFactory.getLogger(ConfirmNode.class);

    @Override
    public Map<String, Object> apply(AppContext state) {
        String message = state.userMessage();
        String normalized = message != null ? message.trim().toUpperCase() : "";

        boolean isConfirmationInput = "CONFIRM".equals(normalized) || "CANCEL".equals(normalized);

        if (isConfirmationInput) {
            if ("CONFIRM".equals(normalized)) {
                log.info("Confirmation received");
                return Map.of(
                        GraphRoutes.CONFIRMATION_PENDING, false,
                        GraphRoutes.AWAITING_CONFIRMATION, false,
                        GraphRoutes.CONFIRMATION_RESULT, "CONFIRM",
                        GraphRoutes.AGENT_REPLY, "Proceeding with your lost/stolen card report..."
                );
            } else {
                log.info("Cancellation received");
                return Map.of(
                        GraphRoutes.CONFIRMATION_PENDING, false,
                        GraphRoutes.AWAITING_CONFIRMATION, false,
                        GraphRoutes.CONFIRMATION_RESULT, "CANCEL",
                        GraphRoutes.AGENT_REPLY, "Your lost/stolen card report has been cancelled."
                );
            }
        }

        log.info("No valid confirmation input, re-prompting");
        return Map.of(
                GraphRoutes.AGENT_REPLY, "Please respond with CONFIRM or CANCEL to proceed with your lost/stolen card report."
        );
    }
}