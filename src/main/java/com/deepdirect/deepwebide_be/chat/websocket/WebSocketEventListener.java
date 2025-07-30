package com.deepdirect.deepwebide_be.chat.websocket;

import com.deepdirect.deepwebide_be.chat.service.ChatSessionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private static final Logger log = LoggerFactory.getLogger(WebSocketEventListener.class);
    private final ChatSessionService chatSessionService;

    @EventListener
    public void handleSessionConnected(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        String repositoryIdHeader = accessor.getFirstNativeHeader("repositoryId");
        String userIdHeader = accessor.getFirstNativeHeader("userId");

        if (repositoryIdHeader == null || userIdHeader == null || sessionId == null) {
            log.warn("WebSocket 접속 시 헤더 누락 (repositoryId: {}, userId: {}, sessionId: {})",
                    repositoryIdHeader, userIdHeader, sessionId);
            return;
        }

        try {
            Long repositoryId = Long.parseLong(repositoryIdHeader);
            Long userId = Long.parseLong(userIdHeader);
            chatSessionService.addSession(repositoryId, userId, sessionId);
            log.info("✅ WebSocket 연결됨: repositoryId={}, userId={}, sessionId={}", repositoryId, userId, sessionId);
        } catch (NumberFormatException e) {
            log.warn("WebSocket 연결 시 잘못된 헤더 형식 (repositoryId: {}, userId: {})", repositoryIdHeader, userIdHeader);
        }
    }

    @EventListener
    public void handleSessionDisconnected(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        if (sessionId != null) {
            chatSessionService.removeSessionBySessionId(sessionId);
            log.info("WebSocket 연결 종료: sessionId={}", sessionId);
        } else {
            log.warn("WebSocket 연결 종료 시 sessionId 누락");
        }
    }
}
