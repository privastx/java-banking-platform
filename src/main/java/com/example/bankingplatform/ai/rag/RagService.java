package com.example.bankingplatform.ai.rag;

import com.example.bankingplatform.ai.chat.RagChat;
import com.example.bankingplatform.ai.chat.models.ChatResponse;
import com.example.bankingplatform.ai.history.ChatHistoryService;
import com.example.bankingplatform.ai.rag.context.ContextBuilder;
import com.example.bankingplatform.ai.rag.retrieval.DocumentRetriever;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

@Service
public class RagService {

    private static final Logger log = LoggerFactory.getLogger(RagService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String FALLBACK = "I don't have enough information to answer that question. Please try rephrasing or contact support.";

    private final DocumentRetriever documentRetriever;
    private final ContextBuilder contextBuilder;
    private final RagChat ragChat;
    private final ChatHistoryService chatHistoryService;
    private final ChatModel chatModel;
    private final String strictPromptTemplate;

    public RagService(DocumentRetriever documentRetriever,
                      ContextBuilder contextBuilder,
                      RagChat ragChat,
                      ChatHistoryService chatHistoryService,
                      @Qualifier("openAiChatModel") ChatModel chatModel,
                      @Value("classpath:prompts/system-prompt-strict.txt") Resource strictPromptResource) {
        this.documentRetriever = documentRetriever;
        this.contextBuilder = contextBuilder;
        this.ragChat = ragChat;
        this.chatHistoryService = chatHistoryService;
        this.chatModel = chatModel;

        try (InputStream is = strictPromptResource.getInputStream()) {
            this.strictPromptTemplate = new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load strict prompt template", e);
        }
    }

    public ChatResponse ask(String sessionId, String question) {
        log.info("Processing RAG query for session={}", sessionId);

        chatHistoryService.addMessage(sessionId, new UserMessage(question));

        List<RagService.RetrievedChunk> retrievedChunks = documentRetriever.retrieve(question);

        if (retrievedChunks.isEmpty()) {
            chatHistoryService.addMessage(sessionId, new AiMessage(FALLBACK));
            return new ChatResponse(
                    sessionId,
                    FALLBACK,
                    null,
                    null,
                    List.of()
            );
        }

        String knowledgeContext = contextBuilder.buildContext(retrievedChunks);
        String userMessage = knowledgeContext + "\n\nQuestion: " + question;

        if (log.isDebugEnabled()) {
            log.debug("RAG userMessage length: {} chars", userMessage.length());
        }

        try {
            String rawResponse = ragChat.chat(sessionId, userMessage);
            ParsedResponse parsed = parseJsonResponse(rawResponse);

            if (isParseFailure(parsed.answer())) {
                log.warn("First attempt failed to produce valid JSON for session={}, retrying with strict prompt", sessionId);
                rawResponse = askWithStrictPrompt(knowledgeContext, question);
                parsed = parseJsonResponse(rawResponse);
            }

            chatHistoryService.addMessage(sessionId, new AiMessage(parsed.answer()));

            return new ChatResponse(
                    sessionId,
                    parsed.answer(),
                    null,
                    null,
                    List.of()
            );
        } catch (Exception e) {
            log.error("Failed to generate response for session={}", sessionId, e);
            String error = "Unable to process your question at this time. Please try again later.";
            chatHistoryService.addMessage(sessionId, new AiMessage(error));
            return new ChatResponse(
                    sessionId,
                    error,
                    null,
                    null,
                    List.of()
            );
        }
    }

    private String askWithStrictPrompt(String knowledgeContext, String question) {
        String strictPrompt = strictPromptTemplate
                .replace("{context}", knowledgeContext)
                .replace("{query}", question);

        return chatModel.chat(strictPrompt);
    }

    private boolean isParseFailure(String answer) {
        return answer != null && answer.contains("I was unable to parse the response");
    }

    private ParsedResponse parseJsonResponse(String response) {
        if (response == null || response.isBlank()) {
            return new ParsedResponse("", "");
        }

        try {
            String json = extractJson(response);
            JsonNode jsonNode = objectMapper.readTree(json);

            String thinking = jsonNode.has("thinking") ? jsonNode.get("thinking").asText() : "";
            String answer = jsonNode.has("answer") ? jsonNode.get("answer").asText() : "I was unable to parse the response. Please try again.";

            if (log.isDebugEnabled()) {
                log.debug("Parsed JSON response: thinking={}, answer={}",
                        thinking != null ? thinking.length() : 0,
                        answer != null ? answer.length() : 0);
            }

            return new ParsedResponse(
                    thinking != null ? thinking : "",
                    answer
            );
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse JSON response, falling back to raw response: {}", e.getMessage());
            return new ParsedResponse("", response.trim());
        }
    }

    private String extractJson(String response) {
        String trimmed = response.trim();

        int startIndex = trimmed.indexOf('{');
        int endIndex = trimmed.lastIndexOf('}');

        if (startIndex == -1 || endIndex == -1 || startIndex >= endIndex) {
            return trimmed;
        }

        return trimmed.substring(startIndex, endIndex + 1);
    }

    public record RetrievedChunk(
            String content,
            String documentId,
            String title,
            String category,
            double similarity
    ) {}

    private record ParsedResponse(
            String thinking,
            String answer
    ) {}
}