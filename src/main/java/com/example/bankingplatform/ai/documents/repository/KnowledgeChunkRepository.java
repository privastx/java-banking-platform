package com.example.bankingplatform.ai.documents.repository;

import com.example.bankingplatform.ai.documents.models.KnowledgeChunk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface KnowledgeChunkRepository extends JpaRepository<KnowledgeChunk, Long> {

    List<KnowledgeChunk> findByDocumentIdOrderByChunkIndexAsc(Long documentId);

    Optional<KnowledgeChunk> findByVectorStoreId(String vectorStoreId);

    void deleteByDocumentId(Long documentId);

    long countByDocumentId(Long documentId);
}