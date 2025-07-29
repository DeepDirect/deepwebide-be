package com.deepdirect.deepwebide_be.sentry;

import com.deepdirect.deepwebide_be.global.security.CustomUserDetails;
import com.deepdirect.deepwebide_be.global.security.JwtTokenProvider;
import com.deepdirect.deepwebide_be.member.domain.User;
import com.deepdirect.deepwebide_be.member.repository.UserRepository;
import io.sentry.Sentry;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class SentryUserContextService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public void setUserContext(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();

            if (principal instanceof CustomUserDetails customUserDetails) {
                setUserContext(customUserDetails);
            }
        }
    }

    public void setUserContext(CustomUserDetails customUserDetails) {
        if (customUserDetails != null) {
            com.deepdirect.deepwebide_be.member.domain.User domainUser = customUserDetails.getUser();

            io.sentry.protocol.User sentryUser = new io.sentry.protocol.User();
            sentryUser.setId(domainUser.getId() != null ? String.valueOf(domainUser.getId()) : null);
            sentryUser.setUsername(domainUser.getUsername());
            sentryUser.setEmail(domainUser.getEmail());

            Sentry.configureScope(scope -> {
                scope.setUser(sentryUser);

                scope.setTag("user_id", domainUser.getId() != null ? String.valueOf(domainUser.getId()) : "unknown");
                scope.setTag("user_email", domainUser.getEmail());
                scope.setTag("user_name", domainUser.getUsername());
                scope.setTag("user_nickname", domainUser.getNickname() != null ? domainUser.getNickname() : "unknown");

                scope.setExtra("user_created_at", domainUser.getCreatedAt() != null ? domainUser.getCreatedAt().toString() : "unknown");
                scope.setExtra("authenticated", String.valueOf(true));
            });

            log.debug("🎭 Sentry에 사용자 정보 설정 완료: {}", domainUser.getUsername() + ": " + domainUser.getNickname());
        }
    }

    // 토큰에서 userId 추출 → DB에서 User 조회 → Sentry에 세팅
    public String setUserContextFromToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            try {
                Long userId = jwtTokenProvider.getUserIdFromToken(token);
                // 1. DB에서 유저 조회
                User user = userRepository.findById(userId)
                        .orElse(null);

                if (user != null) {
                    io.sentry.protocol.User sentryUser = new io.sentry.protocol.User();
                    sentryUser.setId(String.valueOf(user.getId()));
                    sentryUser.setUsername(user.getUsername());
                    sentryUser.setEmail(user.getEmail());

                    Sentry.configureScope(scope -> {
                        scope.setUser(sentryUser);
                        scope.setTag("user_id", String.valueOf(user.getId()));
                        scope.setTag("user_name", user.getUsername());
                        scope.setTag("user_email", user.getEmail());
                        scope.setTag("user_nickname", user.getNickname());
                    });

                    log.debug("🎭 Sentry에 DB기반 사용자 정보 설정 완료: {}", user.getUsername());
                    return user.getUsername() + ": " + user.getNickname();
                }
            } catch (Exception e) {
                log.warn("토큰에서 사용자 정보를 파싱/조회할 수 없습니다: {}", e.getMessage());
            }
        }
        return "unknown";
    }


    public void clearUserContext() {
        Sentry.configureScope(scope -> {
            scope.setUser(null);
            scope.removeTag("user_id");
            scope.removeTag("user_email");
            scope.removeTag("user_name");
            scope.removeExtra("user_created_at");
            scope.removeExtra("authenticated");
        });

        log.debug("🧹 Sentry 사용자 컨텍스트 정리 완료");
    }

    public void setCurrentUserContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        setUserContext(authentication);
    }
}
