package com.deepdirect.deepwebide_be.chat.controller;

import com.deepdirect.deepwebide_be.chat.dto.request.ChatMessageRequest;
import com.deepdirect.deepwebide_be.chat.dto.response.ChatMessageBroadcast;
import com.deepdirect.deepwebide_be.chat.service.ChatMessageWriteService;
import com.deepdirect.deepwebide_be.chat.util.RedisPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

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

        // 2. 메시지 저장 + DTO 응답 변환
        ChatMessageBroadcast broadcast = chatMessageWriteService.saveChatMessage(userId, repositoryId, request.getMessage());

        // 3. Redis 채널로 publish
        redisPublisher.publish("chatroom:" + repositoryId, broadcast);
    }
}