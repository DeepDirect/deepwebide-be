package com.deepdirect.deepwebide_be.sandbox.service;

import com.deepdirect.deepwebide_be.sandbox.dto.request.SandboxExecutionRequest;
import com.deepdirect.deepwebide_be.sandbox.dto.response.SandboxExecutionResponse;
import com.deepdirect.deepwebide_be.sandbox.exception.SandboxException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SandboxService {

    private final RestTemplate restTemplate;

    @Value("${sandbox.api.base-url:http://localhost:9090}")
    private String sandboxBaseUrl;

    public SandboxExecutionResponse requestExecution(SandboxExecutionRequest request) {
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<SandboxExecutionRequest> httpEntity = new HttpEntity<>(request, headers);

            String url = sandboxBaseUrl + "/api/sandbox/run";
            log.info("Requesting sandbox execution: {} with request: {}", url, request);

            ResponseEntity<SandboxExecutionResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    httpEntity,
                    SandboxExecutionResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Sandbox execution completed successfully");
                return response.getBody();
            } else {
                throw new SandboxException("Sandbox execution failed with status: " + response.getStatusCode());
            }

        } catch (RestClientException e) {
            log.error("Failed to communicate with sandbox service", e);
            throw new SandboxException("Sandbox service communication error", e);
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("User-Agent", "DeepWebIDE-Backend");
        return headers;
    }


    /**
     * 컨테이너 중지 요청
     */
    public boolean stopContainer(String uuid) {
        try {
            String url = sandboxBaseUrl + "/api/sandbox/stop/" + uuid;

            log.info("Sending stop request to sandbox server - uuid: {}, url: {}", uuid, url);

            // DELETE 요청으로 컨테이너 중지
            restTemplate.delete(url);

            log.info("Successfully sent stop request for container: {}", uuid);
            return true;

        } catch (RestClientException e) {
            log.error("Failed to send stop request for container: {}", uuid, e);
            return false;
        } catch (Exception e) {
            log.error("Unexpected error while stopping container: {}", uuid, e);
            return false;
        }
    }

    /**
     * 컨테이너 상태 조회
     */
    public Map<String, Object> getContainerStatus(String uuid) {
        try {
            String url = sandboxBaseUrl + "/api/sandbox/status/" + uuid;

            log.debug("Checking container status - uuid: {}, url: {}", uuid, url);

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            log.debug("Container status response: {}", response);
            return response;

        } catch (RestClientException e) {
            log.error("Failed to get container status: {}", uuid, e);
            return Map.of(
                    "uuid", uuid,
                    "status", "ERROR",
                    "error", e.getMessage()
            );
        }
    }
}