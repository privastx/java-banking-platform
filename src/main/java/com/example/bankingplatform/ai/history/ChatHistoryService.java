package com.example.bankingplatform.ai.history;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatHistoryService {

    private static final Logger log = LoggerFactory.getLogger(ChatHistoryService.class);

    private final ChatHistoryRepository chatHistoryRepository;

    public ChatHistoryService(ChatHistoryRepository chatHistoryRepository) {
        this.chatHistoryRepository = chatHistoryRepository;
    }

    @Transactional(readOnly = true)
    public List<ChatHistoryEntity> getHistory(String sessionId) {
        return chatHistoryRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    @Transactional
    public void addMessage(String sessionId, ChatMessage message) {
        ChatHistoryEntity entity = new ChatHistoryEntity();
        entity.setSessionId(sessionId);
        entity.setRole(toRole(message));
        entity.setContent(toContent(message));
        entity.setTokenCount(estimateTokens(message));
        chatHistoryRepository.save(entity);
    }

    @Transactional
    public void addMessages(String sessionId, List<ChatMessage> messages) {
        for (ChatMessage message : messages) {
            addMessage(sessionId, message);
        }
    }

    @Transactional
    public void clearHistory(String sessionId) {
        chatHistoryRepository.deleteBySessionId(sessionId);
    }

    private String toRole(ChatMessage message) {
        if (message instanceof UserMessage) {
            return "USER";
        }
        if (message instanceof AiMessage) {
            return "ASSISTANT";
        }
        return "UNKNOWN";
    }

    private String toContent(ChatMessage message) {
        if (message instanceof UserMessage userMessage) {
            return userMessage.singleText();
        }
        if (message instanceof AiMessage aiMessage) {
            return aiMessage.text();
        }
        return message.toString();
    }

    private int estimateTokens(ChatMessage message) {
        String text = toContent(message);
        return text != null ? text.length() / 4 : 0;
    }
}