package com.example.bankingplatform.ai.agent.models;

import java.io.Serializable;

public record CustomerIdentity(
        Long id,
        String firstName,
        String lastName
) implements Serializable {}