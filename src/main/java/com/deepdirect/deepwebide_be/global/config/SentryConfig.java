package com.deepdirect.deepwebide_be.global.config;

import io.sentry.Sentry;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SentryConfig {

    @Value("${sentry.dsn}")
    private String sentryDsn;

    @Value("${sentry.environment:development}")
    private String sentryEnvironment;

    @Value("${sentry.release:1.0.0}")
    private String sentryRelease;

    @Value("${sentry.debug:true}")
    private boolean sentryDebug;

    @Value("${sentry.traces-sample-rate:1.0}")
    private double sentryTracesSampleRate;

    @PostConstruct
    public void initSentryManually() {
        try {
            log.info("🔧 Sentry 수동 초기화 시작...");

            Sentry.init(options -> {
                options.setDsn(sentryDsn);
                options.setEnvironment(sentryEnvironment);
                options.setRelease(sentryRelease);
                options.setDebug(sentryDebug);
                options.setTracesSampleRate(sentryTracesSampleRate);

//                // 추가 유용한 설정들 (Sentry 8.x 호환)
//                options.setAttachStacktrace(true);
//                options.setAttachThreads(true);
//                options.setSendDefaultPii(false);
                options.setAttachStacktrace(false);
                options.setAttachThreads(false);
                options.setTracesSampleRate(0.1);

                // 성능 모니터링은 traces-sample-rate > 0 이면 자동으로 활성화됨
                log.info("🔧 Sentry 옵션 설정 완료");
            });

            if (Sentry.isEnabled()) {
                log.info("✅ Sentry 초기화 성공!");
                log.info("  - Environment: {}", sentryEnvironment);
                log.info("  - Release: {}", sentryRelease);
                log.info("  - Debug: {}", sentryDebug);
                log.info("  - Traces Sample Rate: {}", sentryTracesSampleRate);
            } else {
                log.error("❌ Sentry 초기화 실패");
            }

        } catch (Exception e) {
            log.error("❌ Sentry 초기화 중 예외 발생: {}", e.getMessage(), e);
        }
    }

//    @EventListener(ApplicationReadyEvent.class)
//    public void sendInitializationTest() {
//        if (Sentry.isEnabled()) {
//            try {
//                // 초기화 확인 메시지
//                Sentry.captureMessage("🚀 애플리케이션 시작 - Sentry 연동 확인", SentryLevel.INFO);
//
//                // 환경 정보와 함께 메시지 전송
//                Sentry.withScope(scope -> {
//                    scope.setTag("startup", "success");
//                    scope.setTag("environment", sentryEnvironment);
//                    scope.setExtra("timestamp", String.valueOf(System.currentTimeMillis()));
//
//                    Sentry.captureMessage("📋 애플리케이션 환경 정보", SentryLevel.INFO);
//                });
//
//                // 테스트 예외도 전송
//                try {
//                    throw new RuntimeException("🧪 Sentry 예외 처리 테스트");
//                } catch (RuntimeException e) {
//                    Sentry.captureException(e);
//                }
//
//                log.info("📤 Sentry 초기화 테스트 메시지 전송 완료");
//
//            } catch (Exception e) {
//                log.error("❌ Sentry 테스트 메시지 전송 실패: {}", e.getMessage(), e);
//            }
//        } else {
//            log.error("❌ Sentry가 비활성화 상태입니다.");
//        }
//    }
}