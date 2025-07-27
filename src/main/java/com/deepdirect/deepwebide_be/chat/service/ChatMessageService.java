package com.deepdirect.deepwebide_be.chat.service;

import com.deepdirect.deepwebide_be.chat.domain.ChatMessage;
import com.deepdirect.deepwebide_be.chat.domain.ChatMessageReference;
import com.deepdirect.deepwebide_be.chat.dto.response.ChatMessageResponse;
import com.deepdirect.deepwebide_be.chat.dto.response.ChatMessagesResponse;
import com.deepdirect.deepwebide_be.chat.dto.response.SenderInfo;
import com.deepdirect.deepwebide_be.chat.repository.ChatMessageReferenceRepository;
import com.deepdirect.deepwebide_be.chat.repository.ChatMessageRepository;
import com.deepdirect.deepwebide_be.global.exception.ErrorCode;
import com.deepdirect.deepwebide_be.global.exception.GlobalException;
import com.deepdirect.deepwebide_be.repository.domain.Repository;
import com.deepdirect.deepwebide_be.repository.repository.RepositoryMemberRepository;
import com.deepdirect.deepwebide_be.repository.repository.RepositoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public ChatMessagesResponse getMessages(Long repositoryId, Long userId) {
        // 1. 레포 존재 확인
        Repository repository = repositoryRepository.findById(repositoryId)
                .orElseThrow(() -> new GlobalException(ErrorCode.REPOSITORY_NOT_FOUND));

        // 2. 접근 권한 확인
        boolean isOwner = repository.getOwner().getId().equals(userId);
        boolean isMember = repositoryMemberRepository.existsByRepositoryIdAndUserIdAndDeletedAtIsNull(repositoryId, userId);
        if (!isOwner && !isMember) {
            throw new GlobalException(ErrorCode.FORBIDDEN);
        }

        // 3. 메시지 조회
        List<ChatMessage> messages = chatMessageRepository.findAllByRepositoryIdOrderBySentAtAsc(repositoryId);

        // 4. 참조 정보 조회
        List<Long> messageIds = messages.stream().map(ChatMessage::getId).toList();
        List<ChatMessageReference> references = referenceRepository.findByChatMessageIdIn(messageIds);

        // 5. 메시지 ID -> 참조 정보 맵핑
        Map<Long, ChatMessageReference> referenceMap = references.stream()
                .collect(Collectors.toMap(
                        ref -> ref.getChatMessage().getId(),
                        Function.identity()
                ));

        // 6. DTO 매핑
        List<ChatMessageResponse> dtoList = messages.stream()
                .map(message -> {
                    ChatMessageReference ref = referenceMap.get(message.getId());
                    return ChatMessageResponse.builder()
                            .id(message.getId())
                            .message(message.getMessage())
                            .sentAt(message.getSentAt())
                            .isReferenced(ref != null)
                            .filePath(ref != null ? ref.getFilePath() : null)
                            .lineNumber(ref != null ? ref.getLineNumber() : null)
                            .sender(SenderInfo.builder()
                                    .userId(message.getSender().getId())
                                    .nickname(message.getSender().getNickname())
                                    .profileImageUrl(message.getSender().getProfileImageUrl())
                                    .build())
                            .build();
                })
                .toList();

        return ChatMessagesResponse.builder()
                .messages(dtoList)
                .build();
    }
}
