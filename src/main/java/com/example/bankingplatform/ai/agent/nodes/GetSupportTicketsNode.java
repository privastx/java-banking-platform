package com.example.bankingplatform.ai.agent.nodes;

import com.example.bankingplatform.ai.agent.AppContext;
import com.example.bankingplatform.ai.agent.GraphRoutes;
import com.example.bankingplatform.ai.agent.tools.GetSupportTicketsTool;
import org.bsc.langgraph4j.action.NodeAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class GetSupportTicketsNode implements NodeAction<AppContext> {

    private static final Logger log = LoggerFactory.getLogger(GetSupportTicketsNode.class);

    private final GetSupportTicketsTool getSupportTicketsTool;

    public GetSupportTicketsNode(GetSupportTicketsTool getSupportTicketsTool) {
        this.getSupportTicketsTool = getSupportTicketsTool;
    }

    @Override
    public Map<String, Object> apply(AppContext state) {
        Long customerId = state.customerIdentity().id();

        try {
            var tickets = getSupportTicketsTool.getSupportTickets(customerId);
            String response = formatTicketResponse(tickets);

            return Map.of(GraphRoutes.AGENT_REPLY, response);
        } catch (Exception e) {
            log.error("Failed to retrieve support tickets for customerId={}", customerId, e);
            return Map.of(GraphRoutes.AGENT_REPLY, "Unable to retrieve your support tickets at this time. Please try again later.");
        }
    }

    private String formatTicketResponse(List<GetSupportTicketsTool.TicketSummary> tickets) {
        if (tickets == null || tickets.isEmpty()) {
            return "You don't have any support tickets.";
        }

        StringBuilder sb = new StringBuilder("Here are your support tickets:\n\n");
        for (var ticket : tickets) {
            sb.append(String.format(
                    "Ticket #%d - %s [%s, Priority: %s, Status: %s] - Created: %s%n",
                    ticket.id(), ticket.subject(), ticket.category(), ticket.priority(), ticket.status(), ticket.createdAt()
            ));
        }
        return sb.toString();
    }
}