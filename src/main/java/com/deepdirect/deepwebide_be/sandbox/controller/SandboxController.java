package com.deepdirect.deepwebide_be.sandbox.controller;

import com.deepdirect.deepwebide_be.sandbox.service.S3Service;
import com.deepdirect.deepwebide_be.sandbox.service.SandboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects")
public class SandboxController {

    private final S3Service s3Service;
    private final SandboxService sandboxService;

    @PostMapping("/execute")
    public ResponseEntity<?> uploadAndRun(
            @RequestParam("file") MultipartFile file,
            @RequestParam("framework") String framework,
            @RequestParam("port") int port
    ) {
        try {
            String uuid = UUID.randomUUID().toString();
            String s3Url = s3Service.upload(file, uuid);

            Map<String, Object> body = Map.of(
                    "uuid", uuid,
                    "url", s3Url,
                    "framework", framework,
                    "port", port
            );

            String sandboxResponse = sandboxService.requestExecution(body);
            return ResponseEntity.ok("요청 완료: " + sandboxResponse);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("실행 실패: " + e.getMessage());
        }
    }
}
