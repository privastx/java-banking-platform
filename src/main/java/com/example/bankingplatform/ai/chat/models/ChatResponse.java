package com.example.bankingplatform.ai.chat.models;

import java.util.List;

public record ChatResponse(
    String sessionId,
    String message,
    String state,
    String workflow,
    List<String> availableActions
) {}