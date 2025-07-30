package com.deepdirect.deepwebide_be.sandbox.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .connectTimeout(Duration.ofSeconds(10))           // 연결 시도 10초
                .readTimeout(Duration.ofMinutes(10))              // 응답 대기 10분
                .build();
    }
}