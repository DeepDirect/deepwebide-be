package com.deepdirect.deepwebide_be.chat.util;

import com.deepdirect.deepwebide_be.chat.dto.response.ChatMessageBroadcast;
import com.deepdirect.deepwebide_be.chat.dto.response.ChatSystemMessageResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final SimpMessageSendingOperations messagingTemplate;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String raw = new String(message.getBody(), StandardCharsets.UTF_8);
            JsonNode root = objectMapper.readTree(raw);

            String type = root.get("type").asText();
            String topic = new String(message.getChannel(), StandardCharsets.UTF_8);
            Long repositoryId = Long.parseLong(topic.split(":")[1]);

            log.info("📩 Redis 메시지 도착! raw: {}", raw);

            if ("CHAT".equals(type)) {
                ChatMessageBroadcast broadcast = objectMapper.treeToValue(root, ChatMessageBroadcast.class);
                ChatMessageBroadcast response = ChatMessageBroadcast.builder()
                        .type(broadcast.getType())
                        .messageId(broadcast.getMessageId())
                        .senderId(broadcast.getSenderId())
                        .senderNickname(broadcast.getSenderNickname())
                        .senderProfileImageUrl(broadcast.getSenderProfileImageUrl())
                        .message(broadcast.getMessage())
                        .codeReference(broadcast.getCodeReference())
                        .sentAt(broadcast.getSentAt())
                        .IsMine(false)
                        .build();

                messagingTemplate.convertAndSend("/sub/repositories/" + repositoryId + "/chat", response);

            } else {
                // USER_JOINED, USER_LEFT
                ChatSystemMessageResponse system = objectMapper.treeToValue(root, ChatSystemMessageResponse.class);
                messagingTemplate.convertAndSend("/topic/repositories/" + repositoryId + "/chat", system);
            }
        } catch (Exception e) {
            log.error("❌ RedisSubscriber: 메시지 처리 중 에러 발생", e);
        }
    }
}