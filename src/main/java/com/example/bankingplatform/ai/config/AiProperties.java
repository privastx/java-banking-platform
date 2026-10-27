package com.example.bankingplatform.ai.config;

import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "ai")
@Validated
public class AiProperties {

    @Getter
    @Setter
    private Rag rag;

    @Getter
    @Setter
    private Http http;

    @Getter
    @Setter
    private Memory memory;

    @Validated
    @Getter
    @Setter
    public static class Rag {
        private Retrieval retrieval;
        private Context context;
        private Chunking chunking;
        private String knowledgeBaseDir;

        @Validated
        @Getter
        @Setter
        public static class Retrieval {
            @Positive
            private int topK;
            private double similarityThreshold;
        }

        @Validated
        @Getter
        @Setter
        public static class Context {
            @Positive
            private int maxTokens;
        }

        @Validated
        @Getter
        @Setter
        public static class Chunking {
            @Positive
            private int size;
            @Positive
            private int overlap;
            @Positive
            private int maxIterations;
        }
    }

    @Validated
    @Getter
    @Setter
    public static class Http {
        @Positive
        private int connectTimeoutSeconds;
    }

    @Validated
    @Getter
    @Setter
    public static class Memory {
        @Positive
        private int maxMessages;
    }
}