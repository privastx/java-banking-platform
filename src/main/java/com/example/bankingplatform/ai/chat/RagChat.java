package com.example.bankingplatform.ai.chat;

import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

@AiService(
    wiringMode = dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT,
    chatModel = "openAiChatModel",
    chatMemoryProvider = "ragChatMemoryProvider"
)
public interface RagChat {

    @SystemMessage(fromResource = "/prompts/system-prompt.txt")
    String chat(
        @MemoryId String sessionId,
        @UserMessage String message
    );
}
