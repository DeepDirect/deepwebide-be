package com.deepdirect.deepwebide_be.chat.repository;

import com.deepdirect.deepwebide_be.chat.domain.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findAllByRepositoryIdOrderBySentAtAsc(Long repositoryId);
}
