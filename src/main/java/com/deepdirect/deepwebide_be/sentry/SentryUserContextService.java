package com.deepdirect.deepwebide_be.sentry;

import com.deepdirect.deepwebide_be.global.security.CustomUserDetails;
import io.sentry.Sentry;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

// 도메인 User는 패키지명 명시
@Service
@Slf4j
public class SentryUserContextService {

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

                scope.setExtra("user_created_at", domainUser.getCreatedAt() != null ? domainUser.getCreatedAt().toString() : "unknown");
                scope.setExtra("authenticated", String.valueOf(true));
            });

            log.debug("🎭 Sentry에 사용자 정보 설정 완료: {}", domainUser.getUsername());
        }
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
