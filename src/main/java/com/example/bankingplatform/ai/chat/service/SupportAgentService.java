package com.example.bankingplatform.ai.chat.service;

import com.example.bankingplatform.ai.agent.AppContext;
import com.example.bankingplatform.ai.agent.models.Workflow;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphInput;
import org.bsc.langgraph4j.RunnableConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SupportAgentService {

    private static final Logger log = LoggerFactory.getLogger(SupportAgentService.class);

    private final CompiledGraph<AppContext> supportGraph;
    private final ConcurrentHashMap<String, AppContext> sessionCache = new ConcurrentHashMap<>();

    public SupportAgentService(CompiledGraph<AppContext> supportGraph) {
        this.supportGraph = supportGraph;
    }

    public AppContext processMessage(String sessionId, Workflow workflow, String message) {
        return processMessage(sessionId, workflow, message, null);
    }

    public AppContext processMessage(String sessionId, Workflow workflow, String message, String confirmationAction) {
        log.info("Processing message for session={}, workflow={}", sessionId, workflow);

        try {
            AppContext cachedState = sessionCache.get(sessionId);

            AppContext.Builder builder = AppContext.builder()
                    .sessionId(sessionId)
                    .workflow(workflow)
                    .userMessage(message)
                    .confirmationAction(confirmationAction);

            if (cachedState != null) {
                log.info("Restoring cached state for session={}: identityResolved={}, awaitingConfirmation={}",
                        sessionId, cachedState.isIdentityResolved(), cachedState.awaitingConfirmation());
                builder.customerIdentity(cachedState.customerIdentity())
                        .awaitingIdentity(cachedState.awaitingIdentity())
                        .awaitingConfirmation(cachedState.awaitingConfirmation())
                        .confirmationPending(cachedState.confirmationPending())
                        .confirmationResult(cachedState.confirmationResult())
                        .agentReply(cachedState.agentReply());
            }

            AppContext initial = builder.build();

            RunnableConfig config = RunnableConfig.builder()
                    .threadId(sessionId)
                    .build();

            Optional<AppContext> result = supportGraph.invoke(GraphInput.args(initial.data()), config);

            AppContext finalState = result.orElse(initial);
            sessionCache.put(sessionId, finalState);

            return finalState;
        } catch (Exception e) {
            log.error("Graph execution failed for session={}", sessionId, e);
            throw e;
        }
    }
}