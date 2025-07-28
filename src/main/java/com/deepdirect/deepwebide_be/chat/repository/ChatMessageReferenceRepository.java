package com.deepdirect.deepwebide_be.chat.repository;

import com.deepdirect.deepwebide_be.chat.domain.ChatMessageReference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageReferenceRepository extends  JpaRepository<ChatMessageReference, Long> {
    //조회된 메세지 목록 기준으로 관련된 참조 정보 한번에 조회
    List<ChatMessageReference> findByChatMessageIdIn(List<Long> chatMessageIds);
}
