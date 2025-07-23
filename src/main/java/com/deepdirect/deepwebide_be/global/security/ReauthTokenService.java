package com.deepdirect.deepwebide_be.global.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class ReauthTokenService {

    private final StringRedisTemplate redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;

    private static final String PREFIX = "reauth:";

    public void save(String key, String token) {
        long expireMillis = 5 * 60 * 1000L; // 5분
        redisTemplate.opsForValue()
                .set(PREFIX + key, token, Duration.ofMillis(expireMillis));
    }

    public String find(String key) {
        return redisTemplate.opsForValue().get(PREFIX + key);
    }

    public void delete(String key) {
        redisTemplate.delete(PREFIX + key);
    }

    public boolean isValid(String key, String token) {
        String stored = find(key);
        return stored != null && stored.equals(token) && jwtTokenProvider.validateToken(token);
    }
}
