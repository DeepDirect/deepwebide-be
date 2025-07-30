package com.deepdirect.deepwebide_be.global.config;

import com.deepdirect.deepwebide_be.chat.util.StompHandler;
import com.deepdirect.deepwebide_be.global.security.WebSocketHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompHandler stompHandler;
    private final WebSocketHandshakeInterceptor handshakeInterceptor;


    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/chat") // ws://localhost:8080/ws/chat
                .addInterceptors(handshakeInterceptor)
                .setAllowedOriginPatterns(
                        "http://localhost:*",     // 개발환경 (모든 포트)
                        "https://localhost:*",    // HTTPS 로컬
                        "https://www.deepdirect.site",  // 프론트엔드
                        "https://api.deepdirect.site"   // API 도메인 (필요시)
                ) //TODO:CORS 허용 (배포시 도메인 지지어)
                .withSockJS(); // SockJS fallback
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
//        registry.enableSimpleBroker("/topic"); // 구독 경로 (브라우저가 받을 때)
        registry.setApplicationDestinationPrefixes("/app"); // 발행 경로 (브라우저가 보낼 때)
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompHandler);
    }
}
