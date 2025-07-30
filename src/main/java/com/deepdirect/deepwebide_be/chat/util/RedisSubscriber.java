package com.deepdirect.deepwebide_be.chat.util;

import com.deepdirect.deepwebide_be.chat.dto.response.ChatMessageBroadcast;
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
            // 1. 메시지 바디를 JSON 문자열로 디코딩
            String raw = new String(message.getBody(), StandardCharsets.UTF_8);

            // 2. JSON → DTO 변환
            ChatMessageBroadcast broadcast = objectMapper.readValue(raw, ChatMessageBroadcast.class);

            // 3. isMine은 false로 변경 (다른 사람들에게 보내는 메시지)
            ChatMessageBroadcast response = ChatMessageBroadcast.builder()
                    .type(broadcast.getType())
                    .messageId(broadcast.getMessageId())
                    .senderId(broadcast.getSenderId())
                    .senderNickname(broadcast.getSenderNickname())
                    .senderProfileImageUrl(broadcast.getSenderProfileImageUrl())
                    .message(broadcast.getMessage())
                    .sentAt(broadcast.getSentAt())
                    .isMine(false)
                    .build();

            String topic = new String(message.getChannel(), StandardCharsets.UTF_8); // e.g. chatroom:5
            Long repositoryId = Long.parseLong(topic.split(":")[1]);

            // 4. 구독 중인 사용자들에게 메시지 전송
            messagingTemplate.convertAndSend(
                    "/sub/repositories/" + repositoryId + "/chat",
                    response
            );

            log.debug("📢 RedisSubscriber: 레포 {} 채팅 메시지 broadcast 완료", repositoryId);
        } catch (Exception e) {
            log.error("❌ RedisSubscriber: 메시지 처리 중 에러 발생", e);
        }
    }
}