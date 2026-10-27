package com.example.bankingplatform.ai.documents;

import com.example.bankingplatform.ai.config.AiProperties;
import com.example.bankingplatform.ai.documents.core.DocumentCategory;
import com.example.bankingplatform.ai.documents.core.MarkdownParser;
import com.example.bankingplatform.ai.documents.core.MarkdownParser.ParsedDocument;
import com.example.bankingplatform.ai.documents.models.KnowledgeChunk;
import com.example.bankingplatform.ai.documents.models.KnowledgeDocument;
import com.example.bankingplatform.ai.documents.repository.KnowledgeDocumentRepository;
import com.example.bankingplatform.ai.documents.repository.KnowledgeChunkRepository;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Service
@EnableConfigurationProperties(AiProperties.class)
public class DocumentIngestionService {

    private static final Logger log = LoggerFactory.getLogger(DocumentIngestionService.class);
    private static final ReentrantLock ingestionLock = new ReentrantLock();

    private final KnowledgeDocumentRepository documentRepository;
    private final KnowledgeChunkRepository chunkRepository;
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final AiProperties aiProperties;
    private final ResourceLoader resourceLoader;
    private final String collectionName;

    public DocumentIngestionService(
            KnowledgeDocumentRepository documentRepository,
            KnowledgeChunkRepository chunkRepository,
            EmbeddingModel embeddingModel,
            EmbeddingStore<TextSegment> embeddingStore,
            AiProperties aiProperties,
            ResourceLoader resourceLoader,
            @Value("${langchain4j.qdrant.embedding-store.collection-name}") String collectionName) {
        this.documentRepository = documentRepository;
        this.chunkRepository = chunkRepository;
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
        this.aiProperties = aiProperties;
        this.resourceLoader = resourceLoader;
        this.collectionName = collectionName;
    }

    public IngestionResult ingestAll(boolean force) {
        if (!ingestionLock.tryLock()) {
            throw new RuntimeException("Ingestion already in progress");
        }

        try {
            log.info("Starting document ingestion (force={})", force);
            if (force) {
                chunkRepository.deleteAll();
                documentRepository.deleteAll();
                clearQdrantCollection();
            }

            long startTime = System.currentTimeMillis();

            Path knowledgeBaseDir = getKnowledgeBasePath();
            List<Path> markdownFiles = findMarkdownFiles(knowledgeBaseDir);

            int ingested = 0;
            int skipped = 0;
            int failed = 0;

            for (Path filePath : markdownFiles) {
                try {
                    if (ingestDocument(filePath, force)) {
                        ingested++;
                    } else {
                        skipped++;
                    }
                } catch (Exception e) {
                    log.error("Failed to ingest document: {}", filePath.getFileName(), e);
                    failed++;
                }
            }

            long duration = System.currentTimeMillis() - startTime;
            log.info("Ingestion completed: {} ingested, {} skipped, {} failed in {}ms",
                    ingested, skipped, failed, duration);

            return new IngestionResult(ingested, skipped, failed);
        } finally {
            ingestionLock.unlock();
        }
    }

    public IngestionResult ingestAll() {
        return ingestAll(true);
    }

    private boolean ingestDocument(Path filePath, boolean force) {
        MarkdownParser parser = new MarkdownParser();
        ParsedDocument parsed = parser.parse(filePath);

        String documentId = filePath.getFileName().toString();

        KnowledgeDocument existingDoc = documentRepository.findByDocumentId(documentId).orElse(null);
        if (!force && existingDoc != null && existingDoc.getChecksum().equals(parsed.checksum())) {
            log.info("Skipping unchanged document: {}", documentId);
            return false;
        }

        int chunkSize = aiProperties.getRag().getChunking().getSize();
        int overlapSize = aiProperties.getRag().getChunking().getOverlap();

        Document document = Document.from(parsed.content());
        List<TextSegment> segments = splitByParagraphs(document.text(), chunkSize, overlapSize);

        KnowledgeDocument doc;
        if (existingDoc != null) {
            doc = existingDoc;
            doc.setTitle(parsed.title());
            doc.setCategory(DocumentCategory.valueOf(parsed.category()));
            doc.setChecksum(parsed.checksum());
            doc.setLastModified(java.time.LocalDateTime.now());
        } else {
            doc = new KnowledgeDocument();
            doc.setDocumentId(documentId);
            doc.setFilename(parsed.filename());
            doc.setTitle(parsed.title());
            doc.setCategory(DocumentCategory.valueOf(parsed.category()));
            doc.setChecksum(parsed.checksum());
            doc.setLastModified(
                    java.time.LocalDateTime.ofInstant(
                            java.time.Instant.ofEpochMilli(parsed.lastModified()),
                            java.time.ZoneId.systemDefault()));
        }

        doc = documentRepository.save(doc);

        if (existingDoc != null) {
            List<KnowledgeChunk> existingChunks = chunkRepository.findByDocumentIdOrderByChunkIndexAsc(doc.getId());
            if (!existingChunks.isEmpty()) {
                for (KnowledgeChunk chunk : existingChunks) {
                    if (chunk.getVectorStoreId() != null) {
                        try {
                            embeddingStore.removeAll(List.of(chunk.getVectorStoreId()));
                        } catch (Exception e) {
                            log.error("Failed to remove chunk {} from Qdrant", chunk.getVectorStoreId(), e);
                        }
                    }
                }
                chunkRepository.deleteAll(existingChunks);
            }
        }

        var response = embeddingModel.embedAll(segments);
        List<Embedding> embeddings = response.content();

        for (int i = 0; i < segments.size(); i++) {
            TextSegment segment = segments.get(i);
            Embedding embedding = embeddings.get(i);

            Map<String, Object> metadataMap = new HashMap<>();
            metadataMap.put("documentId", doc.getDocumentId());
            metadataMap.put("title", doc.getTitle());
            metadataMap.put("category", doc.getCategory().name());
            metadataMap.put("chunkIndex", String.valueOf(i));

            Metadata metadata = Metadata.from(metadataMap);
            TextSegment segmentWithMetadata = TextSegment.from(segment.text(), metadata);

            String pointId = embeddingStore.add(embedding, segmentWithMetadata);

            KnowledgeChunk chunk = new KnowledgeChunk();
            chunk.setDocument(doc);
            chunk.setChunkIndex(i);
            chunk.setContent(segment.text());
            chunk.setContentHash(KnowledgeChunk.computeSha256(segment.text()));
            chunk.setVectorStoreId(pointId);
            chunkRepository.save(chunk);
        }

        log.info("Ingested document: {} -> {} chunks", documentId, segments.size());
        return true;
    }

    private void clearQdrantCollection() {
        log.info("Clearing Qdrant collection: {}", collectionName);

        try {
            if (embeddingStore instanceof QdrantEmbeddingStore qdrantEmbeddingStore) {
                qdrantEmbeddingStore.clearStore();
                log.info("Qdrant collection '{}' cleared successfully", collectionName);
            } else {
                log.warn("EmbeddingStore is not a QdrantEmbeddingStore, skipping clear");
            }
        } catch (Exception e) {
            log.error("Failed to clear Qdrant collection '{}'", collectionName, e);
        }
    }

    public void deleteDocumentEmbeddings(Long documentId) {
        List<KnowledgeChunk> chunks = chunkRepository.findByDocumentIdOrderByChunkIndexAsc(documentId);
        List<String> vectorStoreIds = chunks.stream()
                .map(KnowledgeChunk::getVectorStoreId)
                .filter(id -> id != null)
                .collect(Collectors.toList());

        if (!vectorStoreIds.isEmpty()) {
            try {
                embeddingStore.removeAll(vectorStoreIds);
                log.info("Removed {} chunks from Qdrant for document {}", vectorStoreIds.size(), documentId);
            } catch (Exception e) {
                log.error("Failed to remove chunks from Qdrant for document {}", documentId, e);
            }
        }
    }

    private Path getKnowledgeBasePath() {
        try {
            var resource = resourceLoader.getResource(aiProperties.getRag().getKnowledgeBaseDir());
            if (resource.isFile()) {
                return resource.getFile().toPath();
            }
            return Paths.get(resource.getURI());
        } catch (Exception e) {
            throw new RuntimeException("Failed to resolve knowledge base directory", e);
        }
    }

    private List<Path> findMarkdownFiles(Path directory) {
        try {
            return Files.walk(directory)
                    .filter(p -> p.toString().endsWith(".md") || p.toString().endsWith(".markdown"))
                    .sorted()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Failed to scan knowledge base directory", e);
        }
    }

    private List<TextSegment> splitByParagraphs(String content, int maxChars, int overlapChars) {
        List<TextSegment> segments = new ArrayList<>();
        String[] paragraphs = content.split("\\n\\s*\\n");

        StringBuilder currentChunk = new StringBuilder();
        for (String paragraph : paragraphs) {
            String trimmed = paragraph.strip();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (currentChunk.length() > 0 && currentChunk.length() + trimmed.length() + 2 > maxChars) {
                segments.add(TextSegment.from(currentChunk.toString().strip()));
                if (overlapChars > 0 && currentChunk.length() > overlapChars) {
                    currentChunk = new StringBuilder(currentChunk.substring(currentChunk.length() - overlapChars));
                } else {
                    currentChunk = new StringBuilder();
                }
            }
            if (currentChunk.length() > 0) {
                currentChunk.append("\n\n").append(trimmed);
            } else {
                currentChunk.append(trimmed);
            }
        }
        if (currentChunk.length() > 0) {
            segments.add(TextSegment.from(currentChunk.toString().strip()));
        }
        return segments;
    }

    public record IngestionResult(int ingested, int skipped, int failed) {}
}