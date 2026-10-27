package com.example.bankingplatform.ai.agent.tools;

import com.example.bankingplatform.ai.config.AiProperties;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.agent.tool.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class SearchDocumentTool {

    private static final Logger log = LoggerFactory.getLogger(SearchDocumentTool.class);

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final AiProperties.Rag.Retrieval retrieval;

    public SearchDocumentTool(EmbeddingModel embeddingModel,
                              EmbeddingStore<TextSegment> embeddingStore,
                              AiProperties aiProperties) {
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
        this.retrieval = aiProperties.getRag().getRetrieval();
    }

    @Tool("Search the knowledge base for relevant documents")
    public List<DocumentResult> searchDocuments(String query) {
        log.info("Searching documents for query: {}", query);

        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }

        try {
            Embedding queryEmbedding = embeddingModel.embed(query).content();

            EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding)
                    .maxResults(retrieval.getTopK())
                    .minScore(retrieval.getSimilarityThreshold())
                    .build();

            EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(searchRequest);

            return searchResult.matches().stream()
                    .map(match -> {
                        TextSegment textSegment = match.embedded();
                        Map<String, Object> metadata = textSegment.metadata().toMap();
                        return new DocumentResult(
                                metadata.get("documentId").toString(),
                                metadata.get("title").toString(),
                                metadata.get("category").toString(),
                                match.score());
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to search documents for query: {}", query, e);
            return List.of();
        }
    }

    public record DocumentResult(
            String documentId,
            String title,
            String category,
            double similarity
    ) {}
}