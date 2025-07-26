package com.deepdirect.deepwebide_be.sentry;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

@Component
@Slf4j
public class SentryConnectivityTest {

    @EventListener(ApplicationReadyEvent.class)
    public void testSentryConnectivity() {
        testTcpConnection();
        testHttpConnection();
    }

    private void testTcpConnection() {
        String host = "o4509732778541056.ingest.us.sentry.io";
        int port = 443;

        log.info("🌐 Sentry TCP 연결 테스트 시작: {}:{}", host, port);

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 10000);
            log.info("✅ Sentry TCP 연결 성공");
        } catch (IOException e) {
            log.error("❌ Sentry TCP 연결 실패: {}", e.getMessage());
        }
    }

    private void testHttpConnection() {
        try {
            String url = "https://o4509732778541056.ingest.us.sentry.io/api/4509732797939712/envelope/";
            java.net.HttpURLConnection connection =
                    (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("User-Agent", "sentry-java");

            int responseCode = connection.getResponseCode();
            log.info("🌐 Sentry HTTP 연결 테스트 - 응답 코드: {}", responseCode);

        } catch (Exception e) {
            log.error("❌ Sentry HTTP 연결 테스트 실패: {}", e.getMessage());
        }
    }
}

