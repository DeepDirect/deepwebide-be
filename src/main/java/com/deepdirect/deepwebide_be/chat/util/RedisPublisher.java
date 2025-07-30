package com.deepdirect.deepwebide_be.chat.util;

import com.deepdirect.deepwebide_be.chat.dto.response.ChatMessageBroadcast;
import com.deepdirect.deepwebide_be.chat.dto.response.ChatSystemMessageResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisPublisher implements MessageListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public void publish(String topic, Object message) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            redisTemplate.convertAndSend(topic, jsonMessage);
            log.debug("📡 Redis에 메시지 발행 - topic: {}, payload: {}", topic, jsonMessage);
        } catch (JsonProcessingException e) {
            log.error("❌ Redis 메시지 직렬화 실패", e);
        }
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String payload = new String(message.getBody());
            String topic = new String(message.getChannel());

            log.debug("📥 Redis 구독 메시지 수신 - topic: {}, payload: {}", topic, payload);

            // 타입 추론: system 메시지인지 일반 채팅인지 구분
            if (payload.contains("\"type\":\"CHAT\"")) {
                ChatMessageBroadcast chatMessage = objectMapper.readValue(payload, ChatMessageBroadcast.class);
                messagingTemplate.convertAndSend("/sub/repositories/" + chatMessage.getRepositoryId() + "/chat", chatMessage);
            } else {
                ChatSystemMessageResponse systemMessage = objectMapper.readValue(payload, ChatSystemMessageResponse.class);
                messagingTemplate.convertAndSend("/topic/repositories/" + systemMessage.getRepositoryId() + "/chat", systemMessage);
            }

        } catch (Exception e) {
            log.error("❌ Redis 구독 메시지 처리 실패", e);
        }
    }
}