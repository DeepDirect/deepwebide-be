package com.deepdirect.deepwebide_be.sandbox.controller;

import com.deepdirect.deepwebide_be.global.dto.ApiResponseDto;
import com.deepdirect.deepwebide_be.sandbox.dto.request.SandboxExecutionRequest;
import com.deepdirect.deepwebide_be.sandbox.dto.response.SandboxExecutionResponse;
import com.deepdirect.deepwebide_be.sandbox.service.S3Service;
import com.deepdirect.deepwebide_be.sandbox.service.SandboxService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects")
@Tag(name = "SANDBOX", description = "샌드박스 실행 API")
public class SandboxController {

    private final S3Service s3Service;
    private final SandboxService sandboxService;

    @PostMapping("/execute")
    @Operation(summary = "프로젝트 업로드 및 실행", description = "zip 파일을 업로드하고 샌드박스에서 실행합니다.")
    public ResponseEntity<ApiResponseDto<SandboxExecutionResponse>> uploadAndRun(
            @Parameter(description = "업로드할 zip 파일", required = true)
            @RequestParam("file") MultipartFile file,

            @Parameter(description = "프레임워크 타입", example = "spring", required = true)
            @RequestParam("framework") String framework,

            @Parameter(description = "포트 번호", example = "8080", required = true)
            @RequestParam("port") Integer port
    ) {
        try {
            log.info("Starting project execution - framework: {}, port: {}", framework, port);

            // 1. UUID 생성
            String uuid = UUID.randomUUID().toString();

            // 2. S3에 파일 업로드
            String s3Url = s3Service.upload(file, uuid);
            log.debug("File uploaded to S3 - url: {}", s3Url);

            // 3. 샌드박스 실행 요청 생성
            SandboxExecutionRequest request = SandboxExecutionRequest.builder()
                    .uuid(uuid)
                    .url(s3Url)
                    .framework(framework)
                    .port(port)
                    .build();

            // 4. 샌드박스 서비스 호출
            SandboxExecutionResponse response = sandboxService.requestExecution(request);

            log.info("Project execution completed - uuid: {}, status: {}", uuid, response.getStatus());

            return ResponseEntity.ok(
                    ApiResponseDto.of(200, "프로젝트 실행 요청이 완료되었습니다.", response)
            );

        } catch (Exception e) {
            log.error("Project execution failed - framework: {}, port: {}", framework, port, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.of(500, "실행 실패: " + e.getMessage(), null));
        }
    }
}
