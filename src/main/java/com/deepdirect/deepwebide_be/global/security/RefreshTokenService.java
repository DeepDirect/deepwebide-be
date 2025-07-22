package com.deepdirect.deepwebide_be.global.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final StringRedisTemplate redisTemplate;

    // 리프레시 토큰 저장 (userId를 Long으로 받아 String으로 변환)
    public void save(Long userId, String refreshToken, long expireSeconds) {
        redisTemplate.opsForValue()
                .set(String.valueOf(userId), refreshToken, Duration.ofSeconds(expireSeconds));
    }

    // 리프레시 토큰 조회
    public String findByUserId(Long userId) {
        return redisTemplate.opsForValue().get(String.valueOf(userId));
    }

    // 리프레시 토큰 삭제
    public void delete(Long userId) {
        redisTemplate.delete(String.valueOf(userId));
    }
}
