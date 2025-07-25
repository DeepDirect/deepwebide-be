package com.deepdirect.deepwebide_be.repository.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Schema(description = "레포지토리 입장 권한 확인 응답 DTO")
public class RepositoryAccessCheckResponse {

    @Schema(description = "입장 권한 여부", example = "true")
    private boolean access;

    @Schema(description = "레포지토리 요약 정보 (입장 권한이 있을 경우에만 포함)")
    private RepositorySummary repository;

    @Builder
    public RepositoryAccessCheckResponse(boolean access, RepositorySummary repository) {
        this.access = access;
        this.repository = repository;
    }
}
