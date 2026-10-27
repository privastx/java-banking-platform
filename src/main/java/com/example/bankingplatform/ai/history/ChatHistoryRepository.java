package com.example.bankingplatform.ai.history;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatHistoryRepository extends JpaRepository<ChatHistoryEntity, Long> {

    List<ChatHistoryEntity> findBySessionIdOrderByCreatedAtAsc(String sessionId);

    void deleteBySessionId(String sessionId);
}