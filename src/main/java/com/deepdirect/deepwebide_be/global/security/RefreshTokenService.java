package com.deepdirect.deepwebide_be.global.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
public class RefreshTokenService {
    private final StringRedisTemplate redisTemplate;
    private static final long REFRESH_TOKEN_EXPIRE_DAYS = 14; // 2주

    // 저장
    public void saveRefreshToken(Long userId, String refreshToken) {
        redisTemplate.opsForValue().set(
                getKey(userId),
                refreshToken,
                REFRESH_TOKEN_EXPIRE_DAYS,
                TimeUnit.DAYS
        );
    }
    // 조회
    public String getRefreshToken(Long userId) {
        return redisTemplate.opsForValue().get(getKey(userId));
    }
    // 삭제
    public void deleteRefreshToken(Long userId) {
        redisTemplate.delete(getKey(userId));
    }
    private String getKey(Long userId) {
        return "refresh:" + userId;
    }
}
