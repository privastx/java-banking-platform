package com.example.bankingplatform.ai.agent;

import com.example.bankingplatform.ai.agent.models.Workflow;
import com.example.bankingplatform.ai.agent.nodes.ConfirmNode;
import com.example.bankingplatform.ai.agent.nodes.ConfirmPromptNode;
import com.example.bankingplatform.ai.agent.nodes.CreateTicketNode;
import com.example.bankingplatform.ai.agent.nodes.GetSupportTicketsNode;
import com.example.bankingplatform.ai.agent.nodes.GetTransactionsNode;
import com.example.bankingplatform.ai.agent.nodes.ResolveIdentityNode;
import com.example.bankingplatform.ai.agent.nodes.RespondNode;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.CompileConfig;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.action.AsyncEdgeAction;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.checkpoint.MemorySaver;
import org.bsc.langgraph4j.serializer.std.ObjectStreamStateSerializer;
import org.bsc.langgraph4j.state.AgentStateFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class CustomerSupportGraph {

    private static final Logger log = LoggerFactory.getLogger(CustomerSupportGraph.class);

    private static final AgentStateFactory<AppContext> STATE_FACTORY = AppContext::new;

    @Bean
    public CompiledGraph<AppContext> supportGraph(
            ResolveIdentityNode resolveIdentityNode,
            GetTransactionsNode getTransactionsNode,
            GetSupportTicketsNode getSupportTicketsNode,
            ConfirmPromptNode confirmPromptNode,
            ConfirmNode confirmNode,
            CreateTicketNode createTicketNode,
            RespondNode respondNode) {

        log.info("Building SupportGraph");

        var stateSerializer = new ObjectStreamStateSerializer<AppContext>(STATE_FACTORY);

        var builder = new StateGraph<>(stateSerializer);

        try {
            builder.addNode(GraphRoutes.RESOLVE_IDENTITY, AsyncNodeAction.node_async(resolveIdentityNode));
            builder.addNode(GraphRoutes.GET_TRANSACTIONS, AsyncNodeAction.node_async(getTransactionsNode));
            builder.addNode(GraphRoutes.GET_SUPPORT_TICKETS, AsyncNodeAction.node_async(getSupportTicketsNode));
            builder.addNode(GraphRoutes.CONFIRM_PROMPT, AsyncNodeAction.node_async(confirmPromptNode));
            builder.addNode(GraphRoutes.CONFIRM, AsyncNodeAction.node_async(confirmNode));
            builder.addNode(GraphRoutes.CREATE_TICKET, AsyncNodeAction.node_async(createTicketNode));
            builder.addNode(GraphRoutes.RESPOND, AsyncNodeAction.node_async(respondNode));

            builder.addEdge(StateGraph.START, GraphRoutes.RESOLVE_IDENTITY);

            builder.addConditionalEdges(GraphRoutes.RESOLVE_IDENTITY,
                    AsyncEdgeAction.edge_async(state -> {
                        if (state.awaitingIdentity()) {
                            return GraphRoutes.RESPOND;
                        }
                        if (!state.isIdentityResolved()) {
                            return GraphRoutes.RESOLVE_IDENTITY_LOOP;
                        }
                        if (state.awaitingConfirmation()) {
                            return GraphRoutes.CONFIRM;
                        }
                        Object workflowObj = state.value("workflow").orElse(null);
                        if (workflowObj instanceof Workflow workflow) {
                            if (workflow == Workflow.TRANSACTION_HISTORY) {
                                return GraphRoutes.GET_TRANSACTIONS;
                            } else if (workflow == Workflow.SUPPORT_TICKETS) {
                                return GraphRoutes.GET_SUPPORT_TICKETS;
                            }
                            return GraphRoutes.CONFIRM_PROMPT;
                        }
                        return GraphRoutes.GET_TRANSACTIONS;
                    }),
                    Map.of(GraphRoutes.RESOLVE_IDENTITY_LOOP, GraphRoutes.RESOLVE_IDENTITY_LOOP,
                            GraphRoutes.GET_TRANSACTIONS, GraphRoutes.GET_TRANSACTIONS,
                            GraphRoutes.GET_SUPPORT_TICKETS, GraphRoutes.GET_SUPPORT_TICKETS,
                            GraphRoutes.CONFIRM_PROMPT, GraphRoutes.CONFIRM_PROMPT,
                            GraphRoutes.CONFIRM, GraphRoutes.CONFIRM,
                            GraphRoutes.RESPOND, GraphRoutes.RESPOND));

            builder.addEdge(GraphRoutes.CONFIRM_PROMPT, StateGraph.END);

            builder.addConditionalEdges(GraphRoutes.CONFIRM,
                    AsyncEdgeAction.edge_async(state -> {
                        Object confObj = state.value(GraphRoutes.CONFIRMATION_PENDING).orElse(true);
                        Boolean confirmationPending = confObj instanceof Boolean b ? b : true;
                        if (confirmationPending) {
                            return GraphRoutes.CONFIRM_LOOP;
                        }
                        Object resultObj = state.value(GraphRoutes.CONFIRMATION_RESULT).orElse(null);
                        if ("CANCEL".equals(resultObj)) {
                            return GraphRoutes.RESPOND;
                        }
                        return GraphRoutes.PROCEED_TO_TICKET;
                    }),
                    Map.of(GraphRoutes.CONFIRM_LOOP, GraphRoutes.CONFIRM_LOOP,
                            GraphRoutes.PROCEED_TO_TICKET, GraphRoutes.PROCEED_TO_TICKET,
                            GraphRoutes.RESPOND, GraphRoutes.RESPOND));

            builder.addEdge(GraphRoutes.GET_TRANSACTIONS, GraphRoutes.RESPOND);
            builder.addEdge(GraphRoutes.GET_SUPPORT_TICKETS, GraphRoutes.RESPOND);
            builder.addEdge(GraphRoutes.CREATE_TICKET, GraphRoutes.RESPOND);
            builder.addEdge(GraphRoutes.RESPOND, StateGraph.END);

            return builder.compile(CompileConfig.builder()
                    .checkpointSaver(new MemorySaver())
                    .build());
        } catch (GraphStateException e) {
            throw new RuntimeException("Failed to build SupportGraph", e);
        }
    }
}