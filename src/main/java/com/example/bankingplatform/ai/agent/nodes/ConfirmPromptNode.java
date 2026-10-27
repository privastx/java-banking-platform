package com.example.bankingplatform.ai.agent.nodes;

import com.example.bankingplatform.ai.agent.AppContext;
import com.example.bankingplatform.ai.agent.GraphRoutes;
import org.bsc.langgraph4j.action.NodeAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ConfirmPromptNode implements NodeAction<AppContext> {

    private static final Logger log = LoggerFactory.getLogger(ConfirmPromptNode.class);

    @Override
    public Map<String, Object> apply(AppContext state) {
        log.info("ConfirmPromptNode: Prompting user for confirmation");
        return Map.of(
                GraphRoutes.CONFIRMATION_PENDING, true,
                GraphRoutes.AWAITING_CONFIRMATION, true,
                GraphRoutes.AGENT_REPLY, "Before I can create a support ticket for your lost or stolen card, please confirm by typing CONFIRM or CANCEL."
        );
    }
}