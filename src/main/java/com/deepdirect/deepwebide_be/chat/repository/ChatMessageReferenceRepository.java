package com.deepdirect.deepwebide_be.chat.repository;

import com.deepdirect.deepwebide_be.chat.domain.ChatMessageReference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageReferenceRepository extends  JpaRepository<ChatMessageReference, Long> {
    List<ChatMessageReference> findByChatMessageIdIn(List<Long> chatMessageIds);
}
