package com.example.bankingplatform.ai.chat.controller;

import com.example.bankingplatform.ai.chat.models.ChatRequest;
import com.example.bankingplatform.ai.chat.models.ChatResponse;
import com.example.bankingplatform.ai.chat.service.ChatService;
import com.example.bankingplatform.ai.exception.AiExecutionException;
import com.example.bankingplatform.ai.exception.AiServiceUnavailableException;
import com.example.bankingplatform.ai.exception.InvalidChatRequestException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai/chat")
@Tag(name = "AI Chat API", description = "Unified AI chat endpoint for RAG and workflow conversations")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    @Operation(summary = "Send a message to the AI chat assistant", description = "Unified endpoint for RAG queries and workflow-based conversations")
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        try {
            ChatResponse response = chatService.handleRequest(
                request.sessionId(),
                request.message(),
                request.workflow(),
                request.confirmationAction()
            );
            return ResponseEntity.ok(response);
        } catch (InvalidChatRequestException e) {
            log.warn("Invalid request for session={}", request.sessionId(), e);
            return ResponseEntity.badRequest().build();
        } catch (AiServiceUnavailableException e) {
            log.error("Service unavailable for session={}", request.sessionId(), e);
            return ResponseEntity.status(503).build();
        } catch (AiExecutionException e) {
            log.error("Execution error for session={}", request.sessionId(), e);
            return ResponseEntity.status(500).build();
        } catch (Exception e) {
            log.error("Unexpected error for session={}", request.sessionId(), e);
            return ResponseEntity.status(500).build();
        }
    }
}