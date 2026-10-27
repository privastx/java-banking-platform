package com.example.bankingplatform.ai.exception;

public class InvalidChatRequestException extends RuntimeException {

    public InvalidChatRequestException(String message) {
        super(message);
    }
}