package com.example.bankingplatform.ai.agent.nodes;

import com.example.bankingplatform.ai.agent.AppContext;
import com.example.bankingplatform.ai.agent.GraphRoutes;
import com.example.bankingplatform.ai.agent.tools.CreateSupportTicketTool;
import org.bsc.langgraph4j.action.NodeAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CreateTicketNode implements NodeAction<AppContext> {

    private static final Logger log = LoggerFactory.getLogger(CreateTicketNode.class);

    private final CreateSupportTicketTool createSupportTicketTool;

    public CreateTicketNode(CreateSupportTicketTool createSupportTicketTool) {
        this.createSupportTicketTool = createSupportTicketTool;
    }

    @Override
    public Map<String, Object> apply(AppContext state) {
        Long customerId = state.customerIdentity().id();

        try {
            var result = createSupportTicketTool.createLostCardTicket(customerId);
            String response = String.format(
                    "Your support ticket has been created successfully.%nTicket ID: %s%nStatus: %s%n" +
                    "Our team will review your report and contact you shortly.",
                    result.ticketId(), result.status());

            return Map.of(GraphRoutes.AGENT_REPLY, response);
        } catch (Exception e) {
            log.error("Failed to create support ticket for customerId={}", customerId, e);
            return Map.of(GraphRoutes.AGENT_REPLY, "Unable to create your support ticket at this time. Please try again later.");
        }
    }
}