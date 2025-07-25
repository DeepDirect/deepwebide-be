package com.deepdirect.deepwebide_be;

import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RedisTestRunner implements CommandLineRunner {

    private final StringRedisTemplate redisTemplate;

    public RedisTestRunner(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void run(String... args) {
        try {
            redisTemplate.opsForValue().set("boot-test", "OK", Duration.ofSeconds(10));
            String value = redisTemplate.opsForValue().get("boot-test");
            System.out.println(">>> Redis 연결 성공! 값: " + value);
        } catch (Exception e) {
            System.out.println(">>> Redis 연결 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

