package com.deepdirect.deepwebide_be.global.security;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RedisHash(value = "refreshToken", timeToLive = 60 * 60 * 24 * 14) // 2주 (초 단위)
public class RefreshToken {
    @Id
    private String userId;   // 사용자 ID (Long 타입일 경우 toString 변환)
    private String token;    // 리프레시 토큰 값
}
