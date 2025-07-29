package com.deepdirect.deepwebide_be.sentry;

import io.sentry.Sentry;
import io.sentry.SentryLevel;
import io.sentry.protocol.SentryId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/test")
@Slf4j
public class SentryTestController {

    @GetMapping("/error")
    public ResponseEntity<String> throwError() {
        log.info("🧪 Sentry 테스트 시작");

        try {
            // 1. Sentry 상태 확인
            if (!Sentry.isEnabled()) {
                return ResponseEntity.ok("❌ Sentry가 비활성화되어 있습니다.");
            }

            // 2. 단순 메시지 전송 테스트
            SentryId messageId = Sentry.captureMessage("테스트 메시지 전송 - " + System.currentTimeMillis(), SentryLevel.INFO);
            log.info("📤 Sentry 메시지 전송 완료. ID: {}", messageId);

            // 잠시 대기 (메시지가 전송될 시간을 줌)
            Thread.sleep(1000);

            // 3. 예외 발생 및 전송
            throw new RuntimeException("테스트용 예외 - " + System.currentTimeMillis());

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            return ResponseEntity.ok("테스트 중단됨");
        } catch (Exception e) {
            log.error("🚨 예외 발생: {}", e.getMessage());

            try {
                // 예외 전송 시도
                SentryId sentryId = Sentry.captureException(e);
                log.info("📤 Sentry 예외 전송 완료. ID: {}", sentryId);

                // Sentry 전송 강제 플러시
                Sentry.flush(5000);

                return ResponseEntity.ok("예외가 Sentry에 전송되었습니다. ID: " + sentryId);

            } catch (Exception sentryEx) {
                log.error("❌ Sentry 전송 실패: {}", sentryEx.getMessage(), sentryEx);
                return ResponseEntity.status(500).body("Sentry 전송 실패: " + sentryEx.getMessage());
            }
        }
    }

    @GetMapping("/manual-error")
    public ResponseEntity<String> manualError() {
        // 수동으로 다양한 타입의 이벤트 전송

        // 1. 메시지
        Sentry.captureMessage("수동 테스트 메시지", SentryLevel.WARNING);

        // 2. 커스텀 예외
        Exception customException = new IllegalArgumentException("커스텀 테스트 예외");
        Sentry.captureException(customException);

        // 3. ✅ 올바른 컨텍스트 설정
        Sentry.withScope(scope -> {
            // User 설정
            io.sentry.protocol.User user = new io.sentry.protocol.User();
            user.setId("123");
            user.setUsername("testuser");
            user.setEmail("testuser@example.com");
            scope.setUser(user);

            // Tags 설정
            scope.setTag("component", "test-controller");
            scope.setTag("test_type", "manual");

            // Extra 정보 설정
            scope.setExtra("debug_info", "manual error test");
            scope.setExtra("test_timestamp", String.valueOf(System.currentTimeMillis()));
            scope.setExtra("server_info", "deepwebide-backend");

            // Fingerprint 설정 (선택사항)
            scope.setFingerprint(Arrays.asList("custom", "manual-error"));

            Sentry.captureMessage("컨텍스트가 포함된 테스트", SentryLevel.ERROR);
        });

        return ResponseEntity.ok("다양한 Sentry 이벤트가 전송되었습니다.");
    }

    @GetMapping("/sentry-status")
    public ResponseEntity<Map<String, Object>> sentryStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("sentryEnabled", Sentry.isEnabled());
        status.put("sentryHub", Sentry.getCurrentScopes().toString());

        // ✅ 현재 Sentry 설정 정보 확인
        if (Sentry.isEnabled()) {
            var options = Sentry.getCurrentScopes().getOptions();
            status.put("dsn", options.getDsn());
            status.put("environment", options.getEnvironment());
            status.put("release", options.getRelease());
        }

        log.info("Sentry 상태 확인: {}", status);
        return ResponseEntity.ok(status);
    }
}
