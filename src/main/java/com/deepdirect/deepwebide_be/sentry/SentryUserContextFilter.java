package com.deepdirect.deepwebide_be.sentry;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Component
public class SentryUserContextFilter extends OncePerRequestFilter {

    private final SentryUserContextService sentryUserContextService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // 매 요청마다 UserContext 세팅 (SecurityContextHolder 사용)
            sentryUserContextService.setCurrentUserContext();
            filterChain.doFilter(request, response);
        } finally {
            // 요청 끝나면 UserContext 클리어 (스레드 안전)
            sentryUserContextService.clearUserContext();
        }
    }
}
