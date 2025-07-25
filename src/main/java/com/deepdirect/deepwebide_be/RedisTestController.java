package com.deepdirect.deepwebide_be;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
public class RedisTestController {

    private final StringRedisTemplate redisTemplate;

    public RedisTestController(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/redis-test")
    public String redisTest() {
        try {
            redisTemplate.opsForValue().set("test-key", "hello", Duration.ofSeconds(10));
            String value = redisTemplate.opsForValue().get("test-key");
            return "Redis 연결 성공! 값: " + value;
        } catch (Exception e) {
            return "Redis 연결 실패: " + e.getMessage();
        }
    }
}

