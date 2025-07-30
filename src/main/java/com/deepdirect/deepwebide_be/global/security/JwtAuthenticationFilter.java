package com.deepdirect.deepwebide_be.global.security;

import com.deepdirect.deepwebide_be.global.dto.ApiResponseDto;
import com.deepdirect.deepwebide_be.global.exception.ErrorCode;
import com.deepdirect.deepwebide_be.global.exception.GlobalException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain chain)
            throws ServletException, IOException {

        try {
            log.debug("🧪 JWT 필터 실행 - URI: {}, Authorization: {}, QueryToken: {}",
                    request.getRequestURI(),
                    request.getHeader("Authorization"),
                    request.getParameter("token"));

            String uri = request.getRequestURI();

            if (
                    uri.startsWith("/swagger-ui") ||
                            uri.startsWith("/v3/api-docs") ||
                            uri.startsWith("/swagger-resources") ||
                            uri.startsWith("/webjars") ||
                            uri.startsWith("/h2-console") ||
                            uri.startsWith("/test") ||

                            // 정확하게 허용할 /api/auth 경로만 명시
                            uri.equals("/api/auth/signin") ||
                            uri.equals("/api/auth/signup") ||
                            uri.equals("/api/auth/email/find") ||
                            uri.equals("/api/auth/email/check") ||
                            uri.equals("/api/auth/password/verify-user") ||
//                            uri.equals("/api/auth/password/reset") ||
                            uri.equals("/api/auth/token") ||
                            uri.equals("/api/auth/phone/send-code") ||
                            uri.equals("/api/auth/phone/verify-code") ||
                            uri.equals("/api/auth/email/send-code")

            ) {
                chain.doFilter(request, response);
                return;
            }

            String token = resolveToken(request); // MISSING_TOKEN 발생 가능
            jwtTokenProvider.validateToken(token); // INVALID_TOKEN 발생 가능

            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(String.valueOf(userId));
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            chain.doFilter(request, response);

        } catch (GlobalException ex) {
            response.setStatus(ex.getErrorCode().getStatus().value());
            response.setContentType("application/json;charset=UTF-8");

            ApiResponseDto<?> errorResponse = ApiResponseDto.error(
                    ex.getErrorCode().getStatus().value(),
                    ex.getErrorCode().getMessage()
            );

            new ObjectMapper().writeValue(response.getWriter(), errorResponse);
        }
    }

//    private String resolveToken(HttpServletRequest request) {
//        String bearer = request.getHeader("Authorization");
//        if (bearer == null || !bearer.startsWith("Bearer ")) {
//            throw new GlobalException(ErrorCode.MISSING_TOKEN);
//        }
//        return bearer.substring(7);
//    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");

        // 1. 일반 HTTP 요청: Authorization 헤더에서 토큰 추출
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }

        // 2. WebSocket 연결 시: ?token=Bearer xxx 형식으로 전달됨
        String queryToken = request.getParameter("token");
        if (queryToken != null && queryToken.startsWith("Bearer ")) {
            return queryToken.substring(7);
        }

        throw new GlobalException(ErrorCode.MISSING_TOKEN);
    }

}

