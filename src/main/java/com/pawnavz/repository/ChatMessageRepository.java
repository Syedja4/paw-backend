package com.pawnavz.repository;

import com.pawnavz.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {
    Page<ChatMessage> findBySessionIdOrderByCreatedAtAsc(String sessionId, Pageable pageable);
    List<ChatMessage> findTop50BySessionIdOrderByCreatedAtDesc(String sessionId);
    long countBySessionIdAndIsReadFalseAndSenderType(String sessionId, ChatMessage.SenderType senderType);
}
