package com.deepdirect.deepwebide_be.repository.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "레포지토리 생성 응답 DTO")
public class RepositoryCreateResponse {

    @Schema(description = "레포지토리 고유번호")
    private Long repositoryId;

    @Schema(description = "레포지토리 이름", example = "deepwebide")
    private String repositoryName;

    @Schema(description = "소유자 ID", example = "7")
    private Long ownerId;

    @Schema(description = "소유자 이름", example = "고뇌하는 개발자")
    private String ownerName;

    @Schema(description = "레포지토리 생성 시각", example = "2025-07-21T12:34:56")
    private LocalDateTime createdAt;

}
