package com.deepdirect.deepwebide_be.chat.websocket;

import com.deepdirect.deepwebide_be.chat.service.ChatSessionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final ChatSessionService chatSessionService;

    @EventListener
    public void handleConnectEvent(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();

        // fallback
        if (sessionAttributes == null) {
            Message<?> connectMessage = (Message<?>) accessor.getHeader("simpConnectMessage");
            if (connectMessage != null) {
                StompHeaderAccessor connectAccessor = StompHeaderAccessor.wrap(connectMessage);
                sessionAttributes = connectAccessor.getSessionAttributes();
            }
        }

        if (sessionAttributes == null) {
            log.warn("❗ [CONNECT] 세션 속성이 null입니다. sessionId={}", accessor.getSessionId());
            return;
        }

        Long userId = (Long) sessionAttributes.get("userId");
        Long repositoryId = (Long) sessionAttributes.get("repositoryId");

        chatSessionService.addSession(repositoryId, userId); // 이 안에서 메시지 발행됨

        long count = chatSessionService.getConnectedCount(repositoryId);
        log.info("🟢 [CONNECT] 유저 {}가 레포 {} 채팅방에 입장 (현재 접속자: {})", userId, repositoryId, count);
    }

    @EventListener
    public void handleDisconnectEvent(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();

        if (sessionAttributes == null) {
            log.warn("❗ [DISCONNECT] 세션 속성이 null입니다. sessionId={}", accessor.getSessionId());
            return;
        }

        Long userId = (Long) sessionAttributes.get("userId");
        Long repositoryId = (Long) sessionAttributes.get("repositoryId");

        chatSessionService.removeSession(repositoryId, userId); // 이 안에서 메시지 발행됨

        long count = chatSessionService.getConnectedCount(repositoryId);
        log.info("🔴 [DISCONNECT] 유저 {}가 레포 {} 채팅방에서 퇴장 (현재 접속자: {})", userId, repositoryId, count);
    }
}
