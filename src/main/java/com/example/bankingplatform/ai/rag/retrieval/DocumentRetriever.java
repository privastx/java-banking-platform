package com.example.bankingplatform.ai.rag.retrieval;

import com.example.bankingplatform.ai.rag.RagService;
import com.example.bankingplatform.ai.config.AiProperties;
import com.example.bankingplatform.ai.exception.AiServiceUnavailableException;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DocumentRetriever {

    private static final Logger log = LoggerFactory.getLogger(DocumentRetriever.class);

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final AiProperties.Rag.Retrieval retrieval;

    public DocumentRetriever(EmbeddingModel embeddingModel,
                              EmbeddingStore<TextSegment> embeddingStore,
                              AiProperties aiProperties) {
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
        this.retrieval = aiProperties.getRag().getRetrieval();
    }

    public List<RagService.RetrievedChunk> retrieve(String question) {
        log.info("Retrieving knowledge for question: {}", question);

        if (question == null || question.trim().isEmpty()) {
            return List.of();
        }

        try {
            Embedding questionEmbedding = embeddingModel.embed(question).content();

            EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                    .queryEmbedding(questionEmbedding)
                    .maxResults(retrieval.getTopK())
                    .minScore(retrieval.getSimilarityThreshold())
                    .build();

            EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(searchRequest);

            return searchResult.matches().stream()
                    .map(this::toRetrievedChunk)
                    .collect(Collectors.toList());
        } catch (AiServiceUnavailableException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to retrieve knowledge for question: {}", question, e);
            throw new AiServiceUnavailableException("Failed to search knowledge base: " + e.getMessage(), e);
        }
    }

    private RagService.RetrievedChunk toRetrievedChunk(EmbeddingMatch<TextSegment> match) {
        TextSegment textSegment = match.embedded();
        Map<String, Object> metadata = textSegment.metadata().toMap();

        return new RagService.RetrievedChunk(
                textSegment.text(),
                metadata.get("documentId").toString(),
                metadata.get("title").toString(),
                metadata.get("category").toString(),
                match.score()
        );
    }
}