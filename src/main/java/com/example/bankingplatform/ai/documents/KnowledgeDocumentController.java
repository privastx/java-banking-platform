package com.example.bankingplatform.ai.documents;

import com.example.bankingplatform.ai.documents.core.DocumentCategory;
import com.example.bankingplatform.ai.documents.models.KnowledgeChunk;
import com.example.bankingplatform.ai.documents.models.KnowledgeDocument;
import com.example.bankingplatform.ai.documents.repository.KnowledgeDocumentRepository;
import com.example.bankingplatform.ai.documents.repository.KnowledgeChunkRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/api/kb")
@Tag(name = "Knowledge Base API", description = "Knowledge base document management endpoints")
public class KnowledgeDocumentController {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeDocumentController.class);

    private final KnowledgeDocumentRepository documentRepository;
    private final KnowledgeChunkRepository chunkRepository;
    private final DocumentIngestionService ingestionService;

    public KnowledgeDocumentController(
            KnowledgeDocumentRepository documentRepository,
            KnowledgeChunkRepository chunkRepository,
            DocumentIngestionService ingestionService) {
        this.documentRepository = documentRepository;
        this.chunkRepository = chunkRepository;
        this.ingestionService = ingestionService;
    }

    @GetMapping
    @Operation(summary = "List knowledge base documents")
    public ResponseEntity<List<DocumentDto>> listDocuments(
            @RequestParam(required = false) DocumentCategory category) {
        List<KnowledgeDocument> documents;
        if (category != null) {
            documents = documentRepository.findByCategory(category);
        } else {
            documents = documentRepository.findAllOrderedByIngestedAt();
        }

        List<DocumentDto> result = documents.stream()
                .map(this::toDocumentDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get document by ID with chunks")
    public ResponseEntity<DocumentDetailDto> getDocument(@PathVariable Long id) {
        KnowledgeDocument doc = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + id));

        List<KnowledgeChunk> chunks = chunkRepository.findByDocumentIdOrderByChunkIndexAsc(id);

        DocumentDetailDto dto = new DocumentDetailDto(
                doc.getId(),
                doc.getDocumentId(),
                doc.getFilename(),
                doc.getTitle(),
                doc.getCategory(),
                doc.getChecksum(),
                doc.getLastModified(),
                doc.getIngestedAt(),
                chunks.stream()
                        .map(c -> new ChunkDto(c.getChunkIndex(), c.getContent(), c.getVectorStoreId()))
                        .collect(Collectors.toList())
        );

        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete document and its chunks")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        KnowledgeDocument doc = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + id));

        List<KnowledgeChunk> chunks = chunkRepository.findByDocumentIdOrderByChunkIndexAsc(id);

        ingestionService.deleteDocumentEmbeddings(id);

        chunkRepository.deleteAll(chunks);
        documentRepository.delete(doc);

        log.info("Deleted document: {} with {} chunks", id, chunks.size());
        return ResponseEntity.noContent().build();
    }

@PostMapping("/reindex")
    @Operation(summary = "Reindex knowledge base")
    public ResponseEntity<IngestionResponse> reindex(
            @RequestParam(defaultValue = "true") boolean force) {
        DocumentIngestionService.IngestionResult result = ingestionService.ingestAll(force);

        IngestionResponse response = new IngestionResponse(
                result.ingested(),
                result.skipped(),
                result.failed()
        );

        return ResponseEntity.ok(response);
    }
    private DocumentDto toDocumentDto(KnowledgeDocument doc) {
        long chunkCount = chunkRepository.countByDocumentId(doc.getId());
        return new DocumentDto(
                doc.getId(),
                doc.getDocumentId(),
                doc.getFilename(),
                doc.getTitle(),
                doc.getCategory(),
                doc.getChecksum(),
                doc.getLastModified(),
                doc.getIngestedAt(),
                chunkCount
        );
    }

    public record DocumentDto(
            Long id,
            String documentId,
            String filename,
            String title,
            DocumentCategory category,
            String checksum,
            java.time.LocalDateTime lastModified,
            java.time.LocalDateTime ingestedAt,
            long chunkCount
    ) {}

    public record DocumentDetailDto(
            Long id,
            String documentId,
            String filename,
            String title,
            DocumentCategory category,
            String checksum,
            java.time.LocalDateTime lastModified,
            java.time.LocalDateTime ingestedAt,
            List<ChunkDto> chunks
    ) {}

    public record ChunkDto(
            Integer chunkIndex,
            String content,
            String vectorStoreId
    ) {}

    public record IngestionResponse(
            int ingested,
            int skipped,
            int failed
    ) {}
}