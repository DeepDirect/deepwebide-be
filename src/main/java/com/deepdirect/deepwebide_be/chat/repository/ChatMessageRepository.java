package com.deepdirect.deepwebide_be.chat.repository;

import com.deepdirect.deepwebide_be.chat.domain.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 최초 입장 (after가 없는 경우)
    List<ChatMessage> findByRepositoryIdOrderByIdDesc(Long repositoryId, Pageable pageable);

    // 기존 입장 (after가 있는 경우)
    List<ChatMessage> findByRepositoryIdAndIdGreaterThanOrderByIdAsc(Long repositoryId, Long after, Pageable pageable);

    // 과거 메시지 조회
    List<ChatMessage> findByRepositoryIdAndIdLessThanOrderByIdDesc(Long repositoryId, Long beforeId, Pageable pageable);
}
