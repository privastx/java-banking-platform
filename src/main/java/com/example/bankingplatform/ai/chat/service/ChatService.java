package com.example.bankingplatform.ai.chat.service;

import com.example.bankingplatform.ai.agent.AppContext;
import com.example.bankingplatform.ai.agent.models.Workflow;
import com.example.bankingplatform.ai.chat.models.ChatResponse;
import com.example.bankingplatform.ai.rag.RagService;
import com.example.bankingplatform.ai.exception.AiExecutionException;
import com.example.bankingplatform.ai.exception.AiServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final RagService ragService;
    private final SupportAgentService supportAgentService;

    public ChatService(RagService ragService, SupportAgentService supportAgentService) {
        this.ragService = ragService;
        this.supportAgentService = supportAgentService;
    }

    public ChatResponse handleRequest(String sessionId, String message, String workflow, String confirmationAction) {
        if (workflow != null && !workflow.isBlank()) {
            return handleWorkflow(sessionId, workflow, message, confirmationAction);
        }
        return handleRag(sessionId, message);
    }

    private ChatResponse handleRag(String sessionId, String message) {
        try {
            var ragResponse = ragService.ask(sessionId, message);
            return new ChatResponse(
                sessionId,
                ragResponse.message(),
                null,
                null,
                List.of()
            );
        } catch (AiServiceUnavailableException e) {
            throw e;
        } catch (Exception e) {
            log.error("RAG service failed for session={}", sessionId, e);
            throw new AiExecutionException("Failed to process RAG query", e);
        }
    }

    private ChatResponse handleWorkflow(String sessionId, String workflow, String message, String confirmationAction) {
        try {
            Workflow wf = Workflow.fromValue(workflow);
            AppContext appContext = supportAgentService.processMessage(sessionId, wf, message, confirmationAction);
            return buildResponse(sessionId, appContext, wf);
        } catch (AiServiceUnavailableException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("Invalid workflow: {}", workflow);
            throw new AiExecutionException("Workflow execution failed: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Workflow execution failed for session={}, workflow={}", sessionId, workflow, e);
            throw new AiExecutionException("Workflow execution failed", e);
        }
    }

    private ChatResponse buildResponse(String sessionId, AppContext state, Workflow workflow) {
        String message = state.agentReply();
        if (message == null) {
            message = "I'm sorry, I couldn't process your request at this time.";
        }

        String stateValue = extractStateValue(state);
        List<String> availableActions = extractAvailableActions(state);

        return new ChatResponse(
            sessionId,
            message,
            stateValue,
            workflow.name(),
            availableActions
        );
    }

    private String extractStateValue(AppContext state) {
        if (state.awaitingIdentity()) {
            return "AWAITING_CUSTOMER_IDENTITY";
        }
        if (state.awaitingConfirmation()) {
            return "AWAITING_CONFIRMATION";
        }
        return "COMPLETED";
    }

    private List<String> extractAvailableActions(AppContext state) {
        if (state.awaitingConfirmation()) {
            return List.of("CONFIRM", "CANCEL");
        }
        return List.of();
    }
}