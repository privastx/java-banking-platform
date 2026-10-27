package com.example.bankingplatform.ai.chat.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatRequest(
    @NotBlank String sessionId,
    @NotBlank @Size(max = 1000) String message,
    String workflow,
    String confirmationAction
) {}