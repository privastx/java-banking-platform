package com.example.bankingplatform.ai.documents.repository;

import com.example.bankingplatform.ai.documents.core.DocumentCategory;
import com.example.bankingplatform.ai.documents.models.KnowledgeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface KnowledgeDocumentRepository extends JpaRepository<KnowledgeDocument, Long> {

    Optional<KnowledgeDocument> findByDocumentId(String documentId);

    @Query("SELECT kd FROM KnowledgeDocument kd WHERE :category IS NULL OR kd.category = :category")
    List<KnowledgeDocument> findByCategory(@Param("category") DocumentCategory category);

    @Query("SELECT kd FROM KnowledgeDocument kd ORDER BY kd.ingestedAt DESC")
    List<KnowledgeDocument> findAllOrderedByIngestedAt();
}