package com.deepdirect.deepwebide_be.global.security;

import io.jsonwebtoken.Claims;
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

    public boolean isValid(String key, String token, String username, String email, String phoneNumber, String phoneCode) {
        String stored = find(key);
        if (stored == null || !stored.equals(token)) return false;
        if (!jwtTokenProvider.validateToken(token)) return false;

        Claims claims = jwtTokenProvider.getClaims(token);

        return username.equals(claims.get("username", String.class)) &&
                email.equals(claims.get("email", String.class)) &&
                phoneNumber.equals(claims.get("phoneNumber", String.class)) &&
                phoneCode.equals(claims.get("phoneCode", String.class));
    }
}
