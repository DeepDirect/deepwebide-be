package com.deepdirect.deepwebide_be.repository.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "레포지토리 중지 응답 DTO")
public class RepositoryStopResponse {

    @Schema(description = "레포지토리 ID", example = "1")
    private Long repositoryId;

    @Schema(description = "중지 성공 여부", example = "true")
    private boolean stopped;

    @Schema(description = "중지 메시지", example = "레포지토리가 중지되었습니다.")
    private String message;
}