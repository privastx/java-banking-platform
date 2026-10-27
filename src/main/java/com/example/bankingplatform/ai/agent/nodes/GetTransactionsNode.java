package com.example.bankingplatform.ai.agent.nodes;

import com.example.bankingplatform.ai.agent.AppContext;
import com.example.bankingplatform.ai.agent.GraphRoutes;
import com.example.bankingplatform.ai.agent.tools.GetRecentTransactionsTool;
import org.bsc.langgraph4j.action.NodeAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class GetTransactionsNode implements NodeAction<AppContext> {

    private static final Logger log = LoggerFactory.getLogger(GetTransactionsNode.class);

    private final GetRecentTransactionsTool getRecentTransactionsTool;

    public GetTransactionsNode(GetRecentTransactionsTool getRecentTransactionsTool) {
        this.getRecentTransactionsTool = getRecentTransactionsTool;
    }

    @Override
    public Map<String, Object> apply(AppContext state) {
        Long customerId = state.customerIdentity().id();

        try {
            var transactions = getRecentTransactionsTool.getRecentTransactions(customerId);
            String response = formatTransactionResponse(transactions);

            return Map.of(GraphRoutes.AGENT_REPLY, response);
        } catch (Exception e) {
            log.error("Failed to retrieve transactions for customerId={}", customerId, e);
            return Map.of(GraphRoutes.AGENT_REPLY, "Unable to retrieve your transactions at this time. Please try again later.");
        }
    }

    private String formatTransactionResponse(List<GetRecentTransactionsTool.TransactionSummary> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return "You don't have any recent transactions.";
        }

        StringBuilder sb = new StringBuilder("Here are your 3 most recent transactions:\n\n");
        for (int i = 0; i < transactions.size(); i++) {
            var tx = transactions.get(i);
            sb.append(String.format("%d. %s - %s %s (%s)%n",
                    i + 1, tx.merchantName(), tx.amount(), tx.currency(), tx.transactionTimestamp()));
        }
        return sb.toString();
    }
}