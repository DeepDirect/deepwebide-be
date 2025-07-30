package com.deepdirect.deepwebide_be.global.security;


import com.deepdirect.deepwebide_be.global.exception.ErrorCode;
import com.deepdirect.deepwebide_be.global.exception.GlobalException;
import com.deepdirect.deepwebide_be.member.domain.User;
import com.deepdirect.deepwebide_be.member.repository.UserRepository;
import com.deepdirect.deepwebide_be.repository.domain.Repository;
import com.deepdirect.deepwebide_be.repository.repository.RepositoryMemberRepository;
import com.deepdirect.deepwebide_be.repository.repository.RepositoryRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final RepositoryRepository repositoryRepository;
    private final RepositoryMemberRepository memberRepository;
    private final UserRepository userRepository;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {
        log.debug("🟡 [1] beforeHandshake 진입");

        HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();

        try {
            log.debug("🟡 [2] HttpServletRequest 변환 완료");
            // 1. 쿼리 파라미터에서 token, repositoryId 추출
            String token = servletRequest.getParameter("token");
            String repoIdStr = servletRequest.getParameter("repositoryId");

            log.debug("🟡 [3] 파라미터 추출 - token: {}, repoIdStr: {}", token, repoIdStr);

            if (token == null || repoIdStr == null) {
                log.warn("🛑 [4] 파라미터 누락 - token: {}, repositoryId: {}", token, repoIdStr);
                throw new GlobalException(ErrorCode.MISSING_WS_PARAMS);
            }

            if (token == null) {
                throw new GlobalException(ErrorCode.UNAUTHORIZED);
            }
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Long repositoryId = Long.valueOf(repoIdStr);

            // 2. JWT에서 userId 추출
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            log.debug("🟡 [5] JWT 파싱 완료 - userId: {}", userId);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        log.warn("🛑 [6] 사용자 없음: {}", userId);
                        return new GlobalException(ErrorCode.USER_NOT_FOUND);
                    });

            // 3. 레포가 존재하는지 + soft delete되지 않았는지
            Repository repository = repositoryRepository.findByIdAndDeletedAtIsNull(repositoryId)
                    .orElseThrow(() -> {
                        log.warn("🛑 [7] 레포 없음 or 삭제됨: {}", repositoryId);
                        return new GlobalException(ErrorCode.REPOSITORY_NOT_FOUND);
                    });

            // 4. 공유 상태인지 확인
            if (!repository.isShared()) {
                log.warn("🛑 [8] 레포가 공유 상태가 아님");
                throw new GlobalException(ErrorCode.REPOSITORY_NOT_SHARED);
            }

            // 5. 멤버인지 + soft delete 안된 상태인지
            boolean isValidMember = memberRepository.existsByRepositoryIdAndUserIdAndDeletedAtIsNull(repositoryId, userId);
            if (!isValidMember) {
                log.warn("🛑 [9] 유효한 멤버 아님 - userId: {}", userId);
                throw new GlobalException(ErrorCode.NOT_MEMBER);
            }

            // 6. WebSocket 세션에 사용자 정보 저장
            attributes.put("userId", userId);
            attributes.put("repositoryId", repositoryId);

            log.debug("✅ WebSocket 인증 성공 - 사용자 ID: {}, 레포 ID: {}", userId, repositoryId);
            return true;

        } catch (Exception e) {
            log.warn("❌ WebSocket 인증 실패: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        // 연결 이후 후처리 필요 없음
    }
}