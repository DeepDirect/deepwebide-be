package com.deepdirect.deepwebide_be.chat.service;

import com.deepdirect.deepwebide_be.chat.dto.response.ChatUserEventResponse;
import com.deepdirect.deepwebide_be.chat.dto.response.ChatUserInfoResponse;
import com.deepdirect.deepwebide_be.global.exception.ErrorCode;
import com.deepdirect.deepwebide_be.global.exception.GlobalException;
import com.deepdirect.deepwebide_be.member.domain.User;
import com.deepdirect.deepwebide_be.member.repository.UserRepository;
import com.deepdirect.deepwebide_be.repository.domain.Repository;
import com.deepdirect.deepwebide_be.repository.repository.RepositoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatSessionService {

    private final StringRedisTemplate redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final RepositoryRepository repositoryRepository;

    private static final String SESSION_KEY_PREFIX = "chat_session:";
    private static final String SESSION_META_PREFIX = "chat_session_meta:";

    public void addSession(Long repositoryId, Long userId, String sessionId) {
        Repository repository = repositoryRepository.findByIdAndDeletedAtIsNull(repositoryId)
                .orElseThrow(() -> new GlobalException(ErrorCode.REPOSITORY_NOT_FOUND));

        // 공유되지 않은 레포 차단
        if (!repository.isShared()) {
            throw new GlobalException(ErrorCode.REPOSITORY_NOT_SHARED);
        }

        // Redis에 세션 저장 (세트에 sessionId 저장)
        redisTemplate.opsForSet().add(getSessionKey(repositoryId), sessionId);
        redisTemplate.opsForHash().put(getSessionMetaKey(sessionId), "repositoryId", repositoryId.toString());
        redisTemplate.opsForHash().put(getSessionMetaKey(sessionId), "userId", userId.toString());

        // 접속자 수 조회
        Long activeCount = redisTemplate.opsForSet().size(getSessionKey(repositoryId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));

        // 메시지 브로드캐스트
        ChatUserEventResponse message = ChatUserEventResponse.builder()
                .type("USER_JOINED")
                .repositoryId(repositoryId)
                .user(ChatUserInfoResponse.builder()
                        .userId(user.getId())
                        .nickname(user.getNickname())
                        .build())
                .activeUserCount(activeCount.intValue())
                .message("입장에 성공하였습니다.")
                .timestamp(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSend(
                "/topic/repositories/" + repositoryId + "/chat", message
        );

        log.info("USER_JOINED 전송: repositoryId={}, userId={}, sessionId={}", repositoryId, userId, sessionId);
    }

    public void removeSessionBySessionId(String sessionId) {
        String metaKey = getSessionMetaKey(sessionId);

        String repositoryIdStr = (String) redisTemplate.opsForHash().get(metaKey, "repositoryId");
        String userIdStr = (String) redisTemplate.opsForHash().get(metaKey, "userId");

        if (repositoryIdStr == null || userIdStr == null) {
            log.warn("세션 메타 정보 누락: sessionId={}", sessionId);
            return;
        }

        Long repositoryId = Long.parseLong(repositoryIdStr);
        Long userId = Long.parseLong(userIdStr);

        // Redis에서 세션 제거
        redisTemplate.opsForSet().remove(getSessionKey(repositoryId), sessionId);
        redisTemplate.delete(metaKey);

        Long activeCount = redisTemplate.opsForSet().size(getSessionKey(repositoryId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));

        // 메시지 브로드캐스트
        ChatUserEventResponse message = ChatUserEventResponse.builder()
                .type("USER_LEFT")
                .repositoryId(repositoryId)
                .user(ChatUserInfoResponse.builder()
                        .userId(user.getId())
                        .nickname(user.getNickname())
                        .build())
                .activeUserCount(activeCount.intValue())
                .message("퇴장에 성공하였습니다.")
                .timestamp(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSend(
                "/topic/repositories/" + repositoryId + "/chat", message
        );

        log.info("USER_LEFT 전송: repositoryId={}, userId={}, sessionId={}", repositoryId, userId, sessionId);
    }

    private String getSessionKey(Long repositoryId) {
        return SESSION_KEY_PREFIX + repositoryId;
    }

    private String getSessionMetaKey(String sessionId) {
        return SESSION_META_PREFIX + sessionId;
    }
}