package com.deepdirect.deepwebide_be.sandbox.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects")
public class SandboxLogController {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${sandbox.api.base-url}")
    private String sandboxBaseUrl;

    @GetMapping("/logs/{uuid}")
    public ResponseEntity<String> getContainerLogs(@PathVariable String uuid) {
        String containerId = "sandbox-" + uuid;
        String sandboxUrl = sandboxBaseUrl + "/api/sandbox/logs/" + containerId;

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(sandboxUrl, String.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("샌드박스 로그 조회 실패: " + e.getMessage());
        }
    }
}
