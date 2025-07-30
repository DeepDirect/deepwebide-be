package com.deepdirect.deepwebide_be.chat.controller;

import com.deepdirect.deepwebide_be.chat.dto.request.ChatMessageRequest;
import com.deepdirect.deepwebide_be.chat.service.ChatMessageWriteService;
import com.deepdirect.deepwebide_be.chat.util.RedisPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatMessageWriteService chatMessageWriteService;
    private final RedisPublisher redisPublisher;

    @MessageMapping("/repositories/{repositoryId}/chat/send")
    public void sendMessage(
            @DestinationVariable Long repositoryId,
            ChatMessageRequest request,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        // 1. WebSocket 세션에서 userId 추출
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");

        log.debug("📨 채팅 메시지 수신 - userId: {}, repositoryId: {}, content: {}",
                userId, repositoryId, request.getMessage());

        // 2. 메시지 저장 + DTO 응답 변환
        var broadcast = chatMessageWriteService.saveChatMessage(userId, repositoryId, request.getMessage());

        // 3. Redis 채널로 publish
        redisPublisher.publish("chatroom:" + repositoryId, broadcast);
    }
}