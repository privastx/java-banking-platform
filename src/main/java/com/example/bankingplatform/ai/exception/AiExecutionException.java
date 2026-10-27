package com.example.bankingplatform.ai.exception;

public class AiExecutionException extends RuntimeException {

    public AiExecutionException(String message) {
        super(message);
    }

    public AiExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}