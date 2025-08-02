package com.deepdirect.deepwebide_be.chat.service;

import com.deepdirect.deepwebide_be.chat.domain.ChatMessage;
import com.deepdirect.deepwebide_be.chat.domain.ChatMessageReference;
import com.deepdirect.deepwebide_be.chat.dto.request.ChatMessageRequest;
import com.deepdirect.deepwebide_be.chat.dto.request.CodeReferenceRequest;
import com.deepdirect.deepwebide_be.chat.dto.response.ChatMessageBroadcast;
import com.deepdirect.deepwebide_be.chat.dto.response.CodeReferenceResponse;
import com.deepdirect.deepwebide_be.chat.repository.ChatMessageReferenceRepository;
import com.deepdirect.deepwebide_be.chat.repository.ChatMessageRepository;
import com.deepdirect.deepwebide_be.global.exception.GlobalException;
import com.deepdirect.deepwebide_be.member.domain.User;
import com.deepdirect.deepwebide_be.member.repository.UserRepository;
import com.deepdirect.deepwebide_be.repository.domain.Repository;
import com.deepdirect.deepwebide_be.repository.repository.RepositoryMemberRepository;
import com.deepdirect.deepwebide_be.repository.repository.RepositoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.deepdirect.deepwebide_be.global.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageWriteService {

    private final RepositoryRepository repositoryRepository;
    private final RepositoryMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatMessageReferenceRepository referenceRepository;

    @Transactional
    public ChatMessageBroadcast saveChatMessage(Long userId, ChatMessageRequest request) {
        Long repositoryId = request.getRepositoryId();

        Repository repository = repositoryRepository.findByIdAndDeletedAtIsNull(repositoryId)
                .orElseThrow(() -> new GlobalException(REPOSITORY_NOT_FOUND));

        if (!repository.isShared()) {
            throw new GlobalException(REPOSITORY_NOT_SHARED);
        }

        if (!memberRepository.existsByRepositoryIdAndUserIdAndDeletedAtIsNull(repositoryId, userId)) {
            throw new GlobalException(NOT_MEMBER);
        }

        User sender = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(USER_NOT_FOUND));

        String content = request.getMessage();
        if (content == null || content.isEmpty()) {
            throw new GlobalException(EMPTY_CHAT_MESSAGE);
        }

        // 1. 메시지 저장
        ChatMessage chatMessage = chatMessageRepository.save(
                ChatMessage.of(repository, sender, content)
        );

        // 2. 참조 저장 (있으면)
        CodeReferenceRequest refRequest = request.getCodeReference();
        ChatMessageReference reference = null;
        if (refRequest != null && refRequest.getPath() != null && !refRequest.getPath().isBlank()) {
            reference = referenceRepository.save(ChatMessageReference.builder()
                    .chatMessage(chatMessage)
                    .path(refRequest.getPath())
                    .build());
        }

        // 3. 응답 DTO 변환
        CodeReferenceResponse codeReferenceResponse = reference != null ? CodeReferenceResponse.from(reference) : null;

        return ChatMessageBroadcast.of(chatMessage, sender, repositoryId, codeReferenceResponse, userId);
    }
}
