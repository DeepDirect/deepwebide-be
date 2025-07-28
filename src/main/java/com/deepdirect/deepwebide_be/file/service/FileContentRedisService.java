package com.deepdirect.deepwebide_be.file.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FileContentRedisService {
    private final StringRedisTemplate redisTemplate;

    private static final String REDIS_FILE_CONTENT_PREFIX = "file-content:";

    // 저장
    public void saveFileContentToRedis(Long repositoryId, Long fileId, String content) {
        String key = REDIS_FILE_CONTENT_PREFIX + repositoryId + ":" + fileId;
        redisTemplate.opsForValue().set(key, content);
    }

    // 조회
    public String getFileContentFromRedis(Long repositoryId, Long fileId) {
        String key = REDIS_FILE_CONTENT_PREFIX + repositoryId + ":" + fileId;
        return redisTemplate.opsForValue().get(key);
    }

}
