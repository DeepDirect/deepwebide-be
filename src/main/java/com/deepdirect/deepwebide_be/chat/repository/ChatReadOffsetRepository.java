package com.deepdirect.deepwebide_be.chat.repository;

import com.deepdirect.deepwebide_be.chat.domain.ChatReadOffset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatReadOffsetRepository extends JpaRepository<ChatReadOffset, Long> {

    Optional<ChatReadOffset> findByRepositoryIdAndUserId(Long repositoryId, Long userId);
}
