package com.deepdirect.deepwebide_be.chat.service;

import com.deepdirect.deepwebide_be.chat.domain.ChatMessage;
import com.deepdirect.deepwebide_be.chat.domain.ChatMessageReference;
import com.deepdirect.deepwebide_be.chat.dto.response.ChatMessageResponse;
import com.deepdirect.deepwebide_be.chat.dto.response.ChatMessagesResponse;
import com.deepdirect.deepwebide_be.chat.dto.response.CodeReferenceResponse;
import com.deepdirect.deepwebide_be.chat.repository.ChatMessageReferenceRepository;
import com.deepdirect.deepwebide_be.chat.repository.ChatMessageRepository;
import com.deepdirect.deepwebide_be.global.exception.ErrorCode;
import com.deepdirect.deepwebide_be.global.exception.GlobalException;
import com.deepdirect.deepwebide_be.repository.domain.Repository;
import com.deepdirect.deepwebide_be.repository.repository.RepositoryMemberRepository;
import com.deepdirect.deepwebide_be.repository.repository.RepositoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final RepositoryRepository repositoryRepository;
    private final RepositoryMemberRepository repositoryMemberRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatMessageReferenceRepository referenceRepository;

    @Transactional(readOnly = true)
    public ChatMessagesResponse getMessages(Long repositoryId, Long userId, Long after, Integer size) {
        // 레포 존재 확인
        Repository repository = repositoryRepository.findById(repositoryId)
                .orElseThrow(() -> new GlobalException(ErrorCode.REPOSITORY_NOT_FOUND));

        // 개인 레포는 채팅 불가
        if (!repository.isShared()) {
            throw new GlobalException(ErrorCode.REPOSITORY_NOT_SHARED);
        }

        // 접근 권한 확인
        boolean isOwner = repository.getOwner().getId().equals(userId);
        boolean isMember = repositoryMemberRepository.existsByRepositoryIdAndUserIdAndDeletedAtIsNull(repositoryId, userId);
        if (!isOwner && !isMember) {
            throw new GlobalException(ErrorCode.FORBIDDEN);
        }

        // 채팅 메세지 조회
        Pageable pageable = PageRequest.of(0, size + 1);
        List<ChatMessage> messages = (after == null)
                ? chatMessageRepository.findByRepositoryIdOrderByIdDesc(repositoryId, pageable)
                : chatMessageRepository.findByRepositoryIdAndIdGreaterThanOrderByIdAsc(repositoryId, after, pageable);

        boolean hasMore = messages.size() > size;
        if (hasMore) {
            messages = messages.subList(0, size);
        }

        // 참조 메시지 조회
        List<Long> messageIds = messages.stream().map(ChatMessage::getId).toList();
        Map<Long, List<ChatMessageReference>> referenceMap = referenceRepository
                .findByChatMessageIdIn(messageIds)
                .stream()
                .collect(Collectors.groupingBy(ref -> ref.getChatMessage().getId()));

        // 응답 변환
        List<ChatMessageResponse> responses = messages.stream()
                .map(msg -> ChatMessageResponse.builder()
                        .messageId(msg.getId())
                        .senderId(msg.getSender().getId())
                        .senderNickname(msg.getSender().getNickname())
                        .senderProfileImageUrl(msg.getSender().getProfileImageUrl())
                        .message(msg.getMessage())
                        .codeReferences(referenceMap
                                .getOrDefault(msg.getId(), List.of())
                                .stream()
                                .map(CodeReferenceResponse::from)
                                .toList())
                        .isMine(msg.getSender().getId().equals(userId))
                        .sentAt(msg.getSentAt())
                        .build())
                .toList();

        return ChatMessagesResponse.of(hasMore, responses);
    }
}
