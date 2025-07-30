package com.deepdirect.deepwebide_be.chat.service;

import com.deepdirect.deepwebide_be.chat.domain.ChatMessage;
import com.deepdirect.deepwebide_be.chat.domain.ChatReadOffset;
import com.deepdirect.deepwebide_be.chat.repository.ChatMessageRepository;
import com.deepdirect.deepwebide_be.chat.repository.ChatReadOffsetRepository;
import com.deepdirect.deepwebide_be.global.exception.ErrorCode;
import com.deepdirect.deepwebide_be.global.exception.GlobalException;
import com.deepdirect.deepwebide_be.member.domain.User;
import com.deepdirect.deepwebide_be.member.repository.UserRepository;
import com.deepdirect.deepwebide_be.repository.domain.Repository;
import com.deepdirect.deepwebide_be.repository.repository.RepositoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatReadOffsetService {

    private final ChatReadOffsetRepository chatReadOffsetRepository;
    private final RepositoryRepository repositoryRepository;
    private final UserRepository userRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Transactional
    public void saveOffset(Long repositoryId, Long userId, Long messageId) {
        Repository repository = repositoryRepository.findById(repositoryId)
                .orElseThrow(() -> new GlobalException(ErrorCode.REPOSITORY_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));

        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new GlobalException(ErrorCode.CHAT_MESSAGE_NOT_FOUND));

        chatReadOffsetRepository.findByRepositoryIdAndUserId(repositoryId, userId)
                .ifPresentOrElse(
                        offset -> {
                            if (message.getId() > offset.getLastReadMessage().getId()) {
                                offset.update(message);
                            }
                        },
                        () -> {
                            ChatReadOffset newOffset = ChatReadOffset.of(repository, user, message);
                            chatReadOffsetRepository.save(newOffset);
                        }
                );
    }
}
