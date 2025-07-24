package com.deepdirect.deepwebide_be.repository.dto.response;

import com.deepdirect.deepwebide_be.repository.domain.Repository;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "레포지토리 요약 정보 (리스트조회)")
public class RepositoryResponse {

    @Schema(description = "레포지토리 고유 Id", example = "1")
    private Long repositoryId;

    @Schema(description = "레포지토리 이름", example = "공유한 레포 프로젝트1")
    private String repositoryName;

    @Schema(description = "소유자 Id", example = "5")
    private Long ownerId;

    @Schema(description = "소유자 닉네임", example = "슬기로운 개발자")
    private String ownerName;

    @Schema(description = "공유 여부", example = "true")
    private boolean IsShared;

    @Schema(description = "공유 링크", example = "https://webide.app/repositories1")
    private String shareLink;

    @Schema(description = "생성 일시", example = "2025-07-18T13:10:00Z")
    private LocalDateTime createdAt;

    @Schema(description = "수정 일시", example = "2025-07-22T13:10:00Z")
    private LocalDateTime updatedAt;

    @Schema(description = "즐겨찾기 여부", example = "true")
    private boolean isFavorite;

    public static RepositoryResponse from(Repository repo, boolean isFavorite) {
        return RepositoryResponse.builder()
                .repositoryId(repo.getId())
                .repositoryName(repo.getRepositoryName())
                .ownerId(repo.getOwner().getId())
                .ownerName(repo.getOwner().getNickname())
                .IsShared(repo.isShared())
                .shareLink(repo.getShareLink())
                .createdAt(repo.getCreatedAt())
                .updatedAt(repo.getUpdatedAt())
                .isFavorite(isFavorite)
                .build();
    }
}
