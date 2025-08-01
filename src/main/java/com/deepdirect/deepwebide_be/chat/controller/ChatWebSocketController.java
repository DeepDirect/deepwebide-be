package com.deepdirect.deepwebide_be.chat.controller;

import com.deepdirect.deepwebide_be.chat.dto.request.ChatMessageRequest;
import com.deepdirect.deepwebide_be.chat.dto.response.ChatMessageBroadcast;
import com.deepdirect.deepwebide_be.chat.service.ChatMessageWriteService;
import com.deepdirect.deepwebide_be.chat.util.RedisPublisher;
import com.deepdirect.deepwebide_be.member.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;


@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatMessageWriteService chatMessageWriteService;
    private final RedisPublisher redisPublisher;
    private final UserRepository userRepository;

    @MessageMapping("/repositories/{repositoryId}/chat/send")
    public void sendMessage(
            ChatMessageRequest request,
            SimpMessageHeaderAccessor headerAccessor
    ) {

        // 1. WebSocket 세션에서 userId 추출
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");

        String username = userRepository.findById(userId).get().getUsername();
        log.info("WebSocket 메시지 전송: userId={}, username={}", userId, username);

        System.out.println(request.getMessage());
        // 2. 메시지 저장 + DTO 응답 변환
        ChatMessageBroadcast broadcast = chatMessageWriteService.saveChatMessage(userId, request);

        // 3. Redis 채널로 publish
        redisPublisher.publish("chatroom:" + request.getRepositoryId(), broadcast);
    }
}