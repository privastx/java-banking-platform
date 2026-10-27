package com.example.bankingplatform.ai.agent.nodes;

import com.example.bankingplatform.ai.agent.AppContext;
import com.example.bankingplatform.ai.agent.models.CustomerIdentity;
import com.example.bankingplatform.ai.agent.GraphRoutes;
import com.example.bankingplatform.ai.agent.tools.FindCustomerTool;
import org.bsc.langgraph4j.action.NodeAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ResolveIdentityNode implements NodeAction<AppContext> {

    private static final Logger log = LoggerFactory.getLogger(ResolveIdentityNode.class);

    private final FindCustomerTool findCustomerTool;

    public ResolveIdentityNode(FindCustomerTool findCustomerTool) {
        this.findCustomerTool = findCustomerTool;
    }

    @Override
    public Map<String, Object> apply(AppContext state) {
        String message = state.userMessage();
        if (message != null && message.trim().isEmpty()) {
            message = null;
        }

        String[] nameParts = message != null ? extractNameFromMessage(message) : null;

        if (nameParts == null || nameParts.length != 2) {
            if (state.isIdentityResolved()) {
                log.info("Customer already resolved: customerId={}", state.customerIdentity().id());
                return Map.of();
            }
            HashMap<String, Object> result = new HashMap<>();
            result.put(GraphRoutes.CUSTOMER_IDENTITY, null);
            result.put(GraphRoutes.AWAITING_IDENTITY, true);
            result.put(GraphRoutes.AGENT_REPLY, "Please provide your first and last name so I can look up your account.");
            return result;
        }

        if (state.isIdentityResolved() && state.customerIdentity().firstName().equalsIgnoreCase(nameParts[0])
                && state.customerIdentity().lastName().equalsIgnoreCase(nameParts[1])) {
            log.info("Same customer already resolved: customerId={}", state.customerIdentity().id());
            return Map.of();
        }

        if (state.isIdentityResolved()) {
            log.info("New name provided: {} {}, re-resolving identity", nameParts[0], nameParts[1]);
        }

        CustomerIdentity result = findCustomerTool.findCustomer(nameParts[0], nameParts[1]);
        if (result != null && result.id() != null) {
            log.info("Customer resolved: {} {} -> customerId={}",
                    nameParts[0], nameParts[1], result.id());
            return Map.of(
                    GraphRoutes.CUSTOMER_IDENTITY, new CustomerIdentity(result.id(), result.firstName(), result.lastName()),
                    GraphRoutes.AWAITING_IDENTITY, false
            );
        }

    HashMap<String, Object> failResult = new HashMap<>();
        failResult.put(GraphRoutes.CUSTOMER_IDENTITY, null);
        failResult.put(GraphRoutes.AWAITING_IDENTITY, true);
        failResult.put(GraphRoutes.AGENT_REPLY, "I couldn't find a customer with that name. Please provide your first and last name so I can look up your account.");
        return failResult;
    }

    private String[] extractNameFromMessage(String message) {
        String trimmed = message.trim();
        String[] parts = trimmed.split("\\s+");
        if (parts.length >= 2) {
            return new String[]{parts[0], parts[1]};
        }
        return null;
    }
}