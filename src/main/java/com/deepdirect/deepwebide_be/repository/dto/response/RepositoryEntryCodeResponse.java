package com.deepdirect.deepwebide_be.repository.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "레포 입장 코드 응답")
public class RepositoryEntryCodeResponse {

    @Schema(description = "레포지토리 고유 Id")
    private Long repositoryId;

    @Schema(description = "레포지토리 이름", example = "deepwebide")
    private String repositoryName;

    @Schema(description = "소유자 Id")
    private Long ownerId;

    @Schema(description = "소유자 닉네임", example = "즐거운 개발자")
    private String ownerName;

    @Schema(description = "공유 여부", example = "true")
    private boolean isShared;

    @Schema(description = "공유 링크", example = "https://webide.app/repository1")
    private String shareLink;

    @Schema(description = "입장 코드", example = "Q3irtr53")
    private String entryCode;

    @Schema(description = "생성 일시", example = "2025-07-18T13:10:00Z")
    private LocalDateTime createdAt;

    @Schema(description = "수정 일시", example = "2025-07-22T13:10:00Z")
    private LocalDateTime updatedAt;

}

