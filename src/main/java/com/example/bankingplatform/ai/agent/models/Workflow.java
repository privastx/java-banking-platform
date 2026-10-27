package com.example.bankingplatform.ai.agent.models;

public enum Workflow {
    TRANSACTION_HISTORY,
    LOST_OR_STOLEN_CARD,
    SUPPORT_TICKETS;

    public static Workflow fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Workflow cannot be null");
        }
        return switch (value.toUpperCase()) {
            case "TRANSACTION_HISTORY" -> TRANSACTION_HISTORY;
            case "LOST_OR_STOLEN_CARD" -> LOST_OR_STOLEN_CARD;
            case "SUPPORT_TICKETS" -> SUPPORT_TICKETS;
            default -> throw new IllegalArgumentException("Unknown workflow: " + value);
        };
    }
}