package com.deepdirect.deepwebide_be.repository.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
@Schema(description = "레포지토리 실행 상태 응답 DTO")
public class RepositoryStatusResponse {

    @Schema(description = "레포지토리 ID", example = "1")
    private Long repositoryId;

    @Schema(description = "실행 중인 컨테이너 UUID", example = "sandbox-1a2b3c4d")
    private String uuid;

    @Schema(description = "도커 컨테이너 이름", example = "sandbox-1a2b3c4d")
    private String containerName;

    @Schema(description = "할당된 실행 포트", example = "43210")
    private Integer port;

    @Schema(description = "레포지토리 프레임워크", example = "spring")
    private String framework;

    @Schema(description = "컨테이너 생성 시각", example = "2025-07-30T19:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "DB에 기록된 상태 (예: RUNNING, STOPPED)", example = "RUNNING")
    private String dbStatus;

    @Schema(description = "샌드박스 서버에서 반환한 상태 정보", example = "{\"state\": \"healthy\", \"uptime\": \"5m\"}")
    private Map<String, Object> sandboxStatus;
}