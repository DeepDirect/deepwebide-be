package com.deepdirect.deepwebide_be.global.exception;

import com.deepdirect.deepwebide_be.global.dto.ApiResponseDto;
import com.deepdirect.deepwebide_be.sandbox.exception.SandboxException;
import io.sentry.Sentry;
import io.sentry.SentryLevel;
import io.sentry.protocol.SentryId;
import io.sentry.IScope;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseDto<?>> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("🔶 잘못된 인자: {}", ex.getMessage());
        sendToSentry(ex, "IllegalArgumentException", SentryLevel.WARNING, request);
        return ResponseEntity.badRequest().body(ApiResponseDto.error(400, ex.getMessage()));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponseDto<?>> handleEntityNotFound(EntityNotFoundException ex, HttpServletRequest request) {
        log.warn("🔍 엔티티를 찾을 수 없음: {}", ex.getMessage());
        sendToSentry(ex, "EntityNotFoundException", SentryLevel.WARNING, request);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponseDto.error(404, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("입력값이 유효하지 않습니다.");
        log.warn("🔍 유효성 검증 실패: {}", errorMessage);
        sendValidationErrorToSentry(ex, request);
        return ResponseEntity.badRequest().body(ApiResponseDto.error(400, errorMessage));
    }

    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<ApiResponseDto<?>> handleGlobalException(GlobalException ex, HttpServletRequest request) {
        HttpStatus status = ex.getErrorCode().getStatus();
        if (status.is5xxServerError()) {
            log.error("🚨 서버 오류 (GlobalException): {}", ex.getMessage());
            sendToSentry(ex, "GlobalException", SentryLevel.ERROR, request);
        } else {
            log.warn("⚠️ 클라이언트 오류 (GlobalException): {}", ex.getMessage());
            sendToSentry(ex, "GlobalException", SentryLevel.WARNING, request);
        }
        return ResponseEntity.status(status)
                .body(ApiResponseDto.error(status.value(), ex.getErrorCode().getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<?>> handleOtherExceptions(Exception ex, HttpServletRequest request) {
        log.error("🚨 예상치 못한 서버 오류: {}", ex.getMessage(), ex);
        SentryId sentryId = sendToSentry(ex, "UnexpectedException", SentryLevel.ERROR, request);
        String responseMessage = isDevelopmentMode() ?
                ex.getMessage() + " (Sentry ID: " + sentryId + ")" :
                "서버 내부 오류가 발생했습니다.";
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error(500, responseMessage));
    }

    @ExceptionHandler(SandboxException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleSandboxException(SandboxException e) {
        log.error("Sandbox execution error", e);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponseDto.of(503, "코드 실행 서비스가 일시적으로 사용할 수 없습니다.", null));
    }

    /** Sentry에 예외 정보 전송 */
    private SentryId sendToSentry(Exception ex, String errorType, SentryLevel level, HttpServletRequest request) {
        if (!Sentry.isEnabled()) {
            log.debug("Sentry 비활성화 상태 - 예외 미전송");
            return SentryId.EMPTY_ID;
        }
        try {
            final SentryId[] sentryIdHolder = new SentryId[1];
            Sentry.withScope(scope -> {
                setSentryScope(scope, ex, errorType, level, request);
                SentryId id = Sentry.captureException(ex);
                log.info("📤 Sentry로 예외 전송 완료. ID: {}, Type: {}, Level: {}", id, errorType, level);
                sentryIdHolder[0] = id;
            });
            return sentryIdHolder[0] != null ? sentryIdHolder[0] : SentryId.EMPTY_ID;
        } catch (Exception sentryException) {
            log.error("❌ Sentry 전송 중 오류: {}", sentryException.getMessage(), sentryException);
            return SentryId.EMPTY_ID;
        }
    }


    /** Sentry Scope 상세 설정 */
    private void setSentryScope(IScope scope, Exception ex, String errorType, SentryLevel level, HttpServletRequest request) {
        scope.setTag("error_type", errorType);
        scope.setTag("exception_class", ex.getClass().getSimpleName());
        scope.setLevel(level);

        if (request != null) {
            scope.setTag("http_method", request.getMethod());
            scope.setTag("endpoint", request.getRequestURI());
            scope.setExtra("user_agent", request.getHeader("User-Agent"));
            scope.setExtra("remote_addr", getClientIpAddress(request));
            scope.setExtra("request_url", request.getRequestURL().toString());
            if (request.getQueryString() != null)
                scope.setExtra("query_string", request.getQueryString());
        }

        setUserInfo(scope);

        scope.setExtra("timestamp", String.valueOf(System.currentTimeMillis()));
        scope.setExtra("server_name", getServerName());
        scope.setExtra("thread_name", Thread.currentThread().getName());

        if (ex instanceof GlobalException globalEx) {
            scope.setTag("error_code", globalEx.getErrorCode().name());
            scope.setExtra("http_status", String.valueOf(globalEx.getErrorCode().getStatus().value()));
        }
    }

    /** 유효성 검증 오류를 Sentry에 전송 */
    private void sendValidationErrorToSentry(MethodArgumentNotValidException ex, HttpServletRequest request) {
        if (!Sentry.isEnabled()) return;
        try {
            Sentry.withScope(scope -> {
                scope.setTag("error_type", "ValidationException");
                scope.setLevel(SentryLevel.WARNING);

                if (request != null) {
                    scope.setTag("http_method", request.getMethod());
                    scope.setTag("endpoint", request.getRequestURI());
                }

                Map<String, String> validationErrors = new HashMap<>();
                ex.getBindingResult().getFieldErrors().forEach(error ->
                        validationErrors.put(error.getField(), error.getDefaultMessage())
                );
                scope.setExtra("validation_errors", validationErrors.toString());
                scope.setExtra("rejected_value_count", String.valueOf(ex.getBindingResult().getErrorCount()));
                setUserInfo(scope);

                Sentry.captureException(ex);
                log.info("📤 Validation 오류를 Sentry로 전송 완료");
            });
        } catch (Exception e) {
            log.error("❌ Validation 오류 Sentry 전송 실패: {}", e.getMessage());
        }
    }

    /** 현재 사용자 정보를 Sentry scope에 설정 */
    private void setUserInfo(IScope scope) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                io.sentry.protocol.User user = new io.sentry.protocol.User();
                user.setUsername(auth.getName());
                user.setId(auth.getName());
                scope.setUser(user);
                scope.setExtra("user_authorities", auth.getAuthorities().toString());
            }
        } catch (Exception e) {
            log.debug("사용자 정보 Sentry scope 세팅 실패: {}", e.getMessage());
        }
    }

    /** 클라이언트 실제 IP 주소 획득 */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headers = {
                "X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP",
                "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"
        };
        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }

    private String getServerName() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown";
        }
    }

    /** 현재 환경이 개발 모드인지 확인 */
    private boolean isDevelopmentMode() {
        String profile = System.getProperty("spring.profiles.active", "");
        return profile.contains("dev") || profile.contains("local");
    }
}
