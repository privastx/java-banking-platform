package com.example.bankingplatform.ai.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.http.client.HttpClientBuilder;
import dev.langchain4j.http.client.jdk.JdkHttpClient;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import org.bsc.langgraph4j.checkpoint.MemorySaver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
@EnableConfigurationProperties(AiProperties.class)
public class LangChain4jConfig {

    private static final Logger log = LoggerFactory.getLogger(LangChain4jConfig.class);

    @Value("${langchain4j.qdrant.embedding-store.url}")
    private String qdrantUrl;

    @Value("${langchain4j.qdrant.embedding-store.collection-name}")
    private String collectionName;

    private final AiProperties aiProperties;

    public LangChain4jConfig(AiProperties aiProperties) {
        this.aiProperties = aiProperties;
    }

    /**
     * Configures JDK HttpClient for the OpenAI chat model.
     * HTTP/1.1 is explicitly set to prevent "Unsupported upgrade request" errors
     * on uvicorn-based LLM servers (vllm, llama.cpp, etc.).
     *
     * Bean name MUST be "openAiChatModelHttpClientBuilder" to match the model bean name
     * as required by LangChain4j's Spring Boot auto-configuration.
     *
     * See: https://docs.langchain4j.dev/tutorials/spring-boot-integration#http-client
     * See: https://docs.langchain4j.dev/tutorials/customizable-http-client#http-version
     */
    @Bean("openAiChatModelHttpClientBuilder")
    public HttpClientBuilder openAiChatModelHttpClientBuilder() {
        int timeout = aiProperties.getHttp().getConnectTimeoutSeconds();
        log.info("Configuring JdkHttpClient for openAiChatModel (HTTP/1.1 only, timeout={}s)", timeout);

        HttpClient.Builder jdkBuilder = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(timeout));

        return JdkHttpClient.builder()
                .httpClientBuilder(jdkBuilder);
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        log.info("Configuring Qdrant EmbeddingStore: url={}, collection={}", qdrantUrl, collectionName);

        java.net.URI uri = java.net.URI.create(qdrantUrl);
        String host = uri.getHost() != null ? uri.getHost() : "localhost";
        int httpPort = uri.getPort() > 0 ? uri.getPort() : 6333;
        int grpcPort = httpPort + 1;
        boolean useTls = uri.getScheme().equals("https");

        int timeout = aiProperties.getHttp().getConnectTimeoutSeconds();
        log.info("Creating QdrantClient with gRPC: host={}, grpcPort={}, useTls={}, timeout={}s", host, grpcPort, useTls, timeout);

        QdrantGrpcClient grpcClient = QdrantGrpcClient.newBuilder(host, grpcPort, useTls)
                .withTimeout(Duration.ofSeconds(timeout))
                .build();

        QdrantClient client = new QdrantClient(grpcClient);

        return dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore.builder()
                .client(client)
                .collectionName(collectionName)
                .build();
    }

    @Bean
    public EmbeddingModel embeddingModel() {
        log.info("Configuring All-MiniLM-L6-v2 EmbeddingModel (ONNX)");
        return new AllMiniLmL6V2EmbeddingModel();
    }

    @Bean
    public ChatMemoryProvider ragChatMemoryProvider() {
        int maxMessages = aiProperties.getMemory().getMaxMessages();
        log.info("Configuring ChatMemoryProvider for @AiService RagChat (max {} messages)", maxMessages);
        return memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(maxMessages)
                .build();
    }

    @Bean
    public MemorySaver memorySaver() {
        log.info("Configuring MemorySaver for LangGraph checkpoints");
        return new MemorySaver();
    }
}