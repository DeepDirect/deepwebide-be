package com.deepdirect.deepwebide_be.chat.service;

import com.deepdirect.deepwebide_be.chat.domain.ChatMessage;
import com.deepdirect.deepwebide_be.chat.domain.ChatMessageReference;
import com.deepdirect.deepwebide_be.chat.dto.response.ChatMessageResponse;
import com.deepdirect.deepwebide_be.chat.dto.response.ChatMessageSearchResponse;
import com.deepdirect.deepwebide_be.chat.dto.response.ChatMessagesResponse;
import com.deepdirect.deepwebide_be.chat.dto.response.CodePathListResponse;
import com.deepdirect.deepwebide_be.chat.dto.response.CodeReferenceResponse;
import com.deepdirect.deepwebide_be.chat.repository.ChatMessageReferenceRepository;
import com.deepdirect.deepwebide_be.chat.repository.ChatMessageRepository;
import com.deepdirect.deepwebide_be.file.domain.FileNode;
import com.deepdirect.deepwebide_be.file.domain.FileType;
import com.deepdirect.deepwebide_be.file.repository.FileNodeRepository;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final RepositoryRepository repositoryRepository;
    private final RepositoryMemberRepository repositoryMemberRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatMessageReferenceRepository referenceRepository;
    private final FileNodeRepository fileNodeRepository;

    @Transactional(readOnly = true)
    public ChatMessagesResponse getMessages(Long repositoryId, Long userId, Long before, Long after, Integer size) {
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
        List<ChatMessage> messages;

        if (before != null) {
            messages = chatMessageRepository.findByRepositoryIdAndIdLessThanOrderByIdDesc(repositoryId, before, pageable);
        } else if (after != null) {
            messages = chatMessageRepository.findByRepositoryIdAndIdGreaterThanOrderByIdAsc(repositoryId, after, pageable);
        } else {
            messages = chatMessageRepository.findByRepositoryIdOrderByIdDesc(repositoryId, pageable);
        }

        boolean hasMore = messages.size() > size;
        if (hasMore) {
            messages = messages.subList(0, size);
        }

        // 참조 메시지 조회
        List<Long> messageIds = messages.stream().map(ChatMessage::getId).toList();
        Map<Long, ChatMessageReference> referenceMap = referenceRepository
                .findByChatMessageIdIn(messageIds)
                .stream()
                .collect(Collectors.toMap(
                        ref -> ref.getChatMessage().getId(),
                        Function.identity()
                ));


        // 응답 변환
        List<ChatMessageResponse> responses = messages.stream()
                .map(msg -> ChatMessageResponse.builder()
                        .messageId(msg.getId())
                        .senderId(msg.getSender().getId())
                        .senderNickname(msg.getSender().getNickname())
                        .senderProfileImageUrl(msg.getSender().getProfileImageUrl())
                        .message(msg.getMessage())
                        .codeReference(Optional.ofNullable(referenceMap.get(msg.getId()))
                                .map(CodeReferenceResponse::from)
                                .orElse(null))
                        .isMine(msg.getSender().getId().equals(userId))
                        .sentAt(msg.getSentAt())
                        .build())
                .toList();

        return ChatMessagesResponse.of(hasMore, responses);
    }

    @Transactional(readOnly = true)
    public ChatMessageSearchResponse searchMessages(Long repositoryId, Long userId, String keyword, int size) {
        if (keyword == null || keyword.isBlank()) {
            throw new GlobalException(ErrorCode.INVALID_INPUT);
        }

        Repository repo = repositoryRepository.findById(repositoryId)
                .orElseThrow(() -> new GlobalException(ErrorCode.REPOSITORY_NOT_FOUND));

        if (!repo.isShared() || (!repositoryMemberRepository.existsByRepositoryIdAndUserIdAndDeletedAtIsNull(repositoryId, userId)
                && !repo.getOwner().getId().equals(userId))) {
            throw new GlobalException(ErrorCode.FORBIDDEN);
        }

        Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Order.desc("sentAt"), Sort.Order.desc("id")));
        List<ChatMessage> result = chatMessageRepository
                .findByRepositoryIdAndMessageContainingIgnoreCase(repositoryId, keyword, pageable);

        long total = chatMessageRepository.countByRepositoryIdAndMessageContainingIgnoreCase(repositoryId, keyword);

        Map<Long, ChatMessageReference> ref = referenceRepository
                .findByChatMessageIdIn(result.stream().map(ChatMessage::getId).toList())
                .stream()
                .collect(Collectors.toMap(r -> r.getChatMessage().getId(), Function.identity()
                ));

        List<ChatMessageResponse> responses = result.stream().map(msg ->
                ChatMessageResponse.builder()
                        .messageId(msg.getId())
                        .senderId(msg.getSender().getId())
                        .senderNickname(msg.getSender().getNickname())
                        .senderProfileImageUrl(msg.getSender().getProfileImageUrl())
                        .message(msg.getMessage())
                        .codeReference(Optional.ofNullable(ref.get(msg.getId()))
                                .map(CodeReferenceResponse::from)
                                .orElse(null))
                        .isMine(msg.getSender().getId().equals(userId))
                        .sentAt(msg.getSentAt())
                        .build()
        ).toList();

        return ChatMessageSearchResponse.builder()
                .keyword(keyword)
                .totalElements(total)
                .messages(responses)
                .build();
    }

    @Transactional(readOnly = true)
    public CodePathListResponse getCodePaths(Long repositoryId, Long userId) {
        Repository repository = repositoryRepository.findById(repositoryId)
                .orElseThrow(() -> new GlobalException(ErrorCode.REPOSITORY_NOT_FOUND));

        if (!repository.isShared()) {
            throw new GlobalException(ErrorCode.REPOSITORY_NOT_SHARED);
        }

        boolean isOwner = repository.getOwner().getId().equals(userId);
        boolean isMember = repositoryMemberRepository.existsByRepositoryIdAndUserIdAndDeletedAtIsNull(repositoryId, userId);
        if (!isOwner && !isMember) {
            throw new GlobalException(ErrorCode.FORBIDDEN);
        }

        List<FileNode> fileNodes = fileNodeRepository.findAllByRepositoryId(repositoryId);

        List<String> paths = fileNodes.stream()
                .filter(node -> node.getFileType() == FileType.FILE) // ← 폴더는 제외
                .map(FileNode::getPath)
                .sorted(String::compareToIgnoreCase)
                .toList();

        return CodePathListResponse.builder().paths(paths).build();
    }

}