package com.deepdirect.deepwebide_be.file.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FileContentRedisService {
    private final RedisTemplate<String, String> redisTemplate;

    public void saveFileContent(Long repositoryId, Long fileId, String content) {
        String key = getFileContentKey(repositoryId, fileId);
        redisTemplate.opsForValue().set(key, content);
    }

    public String getFileContent(Long repositoryId, Long fileId) {
        String key = getFileContentKey(repositoryId, fileId);
        return redisTemplate.opsForValue().get(key);
    }

    private String getFileContentKey(Long repositoryId, Long fileId) {
        return "file-content:" + repositoryId + ":" + fileId;
    }
}
