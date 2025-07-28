package com.deepdirect.deepwebide_be.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class ChatSessionService {

    private final StringRedisTemplate redisTemplate;

    private static final String SESSION_KEY_PREFIX = "chat_session:"; // 세션 사용자 목록
    private static final String SESSION_MAPPING_PREFIX = "session_mapping:"; // sessionId → repositoryId:userId

    public void addSession(Long repositoryId, Long userId, String sessionId) {
        // 사용자 등록
        String repoKey = SESSION_KEY_PREFIX + repositoryId;
        redisTemplate.opsForSet().add(repoKey, String.valueOf(userId));

        // sessionId -> repoId:userId 매핑
        String mappingKey = SESSION_MAPPING_PREFIX + sessionId;
        String mappingValue = repositoryId + ":" + userId;
        redisTemplate.opsForValue().set(mappingKey, mappingValue);
    }

    public void removeSessionBySessionId(String sessionId) {
        String mappingKey = SESSION_MAPPING_PREFIX + sessionId;
        String value = redisTemplate.opsForValue().get(mappingKey);

        if (value != null && value.contains(":")) {
            String[] parts = value.split(":");
            Long repositoryId = Long.parseLong(parts[0]);
            Long userId = Long.parseLong(parts[1]);

            // 사용자 제거
            String repoKey = SESSION_KEY_PREFIX + repositoryId;
            redisTemplate.opsForSet().remove(repoKey, String.valueOf(userId));
        }

        // 세션 매핑 삭제
        redisTemplate.delete(mappingKey);
    }

    public Long getSessionCount(Long repositoryId) {
        String key = SESSION_KEY_PREFIX + repositoryId;
        return redisTemplate.opsForSet().size(key);
    }
}

