package com.deepdirect.deepwebide_be.repository.controller;

import com.deepdirect.deepwebide_be.global.dto.ApiResponseDto;
import com.deepdirect.deepwebide_be.global.security.CustomUserDetails;
import com.deepdirect.deepwebide_be.repository.dto.response.RepositoryExecuteResponse;
import com.deepdirect.deepwebide_be.repository.dto.response.RepositoryStatusResponse;
import com.deepdirect.deepwebide_be.repository.dto.response.RepositoryStopResponse;
import com.deepdirect.deepwebide_be.repository.service.AutoStopSchedulerService;
import com.deepdirect.deepwebide_be.repository.service.RepositoryRunService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.Map;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/repositories")
@Tag(name = "RUN", description = "레포지토리 실행, 로그 반환 등 기능 API")
public class RepositoryRunController {

    private final RepositoryRunService repositoryRunService;
    private final AutoStopSchedulerService autoStopSchedulerService;

    @PostMapping("/{repositoryId}/execute")
    @Operation(summary = "레포지토리 실행", description = "레포지토리를 실행하고 실행 결과를 반환합니다.")
    public ResponseEntity<ApiResponseDto<RepositoryExecuteResponse>> executeRepository(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long repositoryId
    ) {
        RepositoryExecuteResponse resp = repositoryRunService.executeRepository(repositoryId, userDetails.getId());
        autoStopSchedulerService.scheduleAutoStop(repositoryId, resp.getUuid(), 10);
        return ResponseEntity.ok(ApiResponseDto.of(200, "레포지토리 실행 요청 완료", resp));
    }

    /**
     * 레포지토리 중지
     */
    @DeleteMapping("/{repositoryId}/stop")
    @Operation(summary = "레포지토리 중지", description = "레포지토리를 중지하고 중지 결과를 반환합니다.")
    public ResponseEntity<ApiResponseDto<RepositoryStopResponse>> stopRepository(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long repositoryId
    ) {
        boolean success = repositoryRunService.stopRepository(repositoryId, userDetails.getId());

        RepositoryStopResponse result = RepositoryStopResponse.builder()
                .repositoryId(repositoryId)
                .stopped(success)
                .message(success ? "레포지토리가 중지되었습니다." : "중지할 컨테이너가 없습니다.")
                .build();

        return ResponseEntity.ok(ApiResponseDto.of(200, "레포지토리 중지 요청 완료", result));
    }

    /**
     * 레포지토리 실행 상태 조회
     */
    @GetMapping("/{repositoryId}/status")
    @Operation(summary = "레포지토리 상태 조회", description = "레포지토리의 실행 상태를 조회합니다.")
    public ResponseEntity<ApiResponseDto<RepositoryStatusResponse>> getRepositoryStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long repositoryId
    ) {
        RepositoryStatusResponse status = repositoryRunService.getRepositoryStatus(repositoryId, userDetails.getId());
        return ResponseEntity.ok(ApiResponseDto.of(200, "레포지토리 상태 조회 완료", status));
    }

    /**
     * 레포지토리 실행 로그 조회
     */
    @GetMapping("/{repositoryId}/logs")
    @Operation(summary = "레포지토리 로그 조회", description = "레포지토리의 실행 로그를 조회합니다.")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> getRepositoryLogs(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long repositoryId,
            @RequestParam(defaultValue = "50") int lines,
            @RequestParam(defaultValue = "5m") String since) {

        Map<String, Object> logs = repositoryRunService.getRepositoryLogs(repositoryId, userDetails.getId(), lines, since);
        return ResponseEntity.ok(ApiResponseDto.of(200, "레포지토리 로그 조회 완료", logs));
    }
}

