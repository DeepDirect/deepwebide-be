package com.deepdirect.deepwebide_be.repository.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "레포지토리 이름 수정 응답 DTO")
public class RepositoryRenameResponse {

    @Schema(description = "레포지토리 고유 Id", example = "1")
    private Long repositoryId;

    @Schema(description = "레포지토리 이름", example = "공유한 레포 프로젝트1")
    private String repositoryName;

    @Schema(description = "소유자 Id", example = "5")
    private Long ownerId;

    @Schema(description = "소유자 닉네임", example = "슬기로운 개발자")
    private String ownerName;

    @Schema(description = "생성 일시", example = "2025-07-18T13:10:00Z")
    private LocalDateTime createdAt;

    @Schema(description = "수정 일시", example = "2025-07-22T13:10:00Z")
    private LocalDateTime updatedAt;
}