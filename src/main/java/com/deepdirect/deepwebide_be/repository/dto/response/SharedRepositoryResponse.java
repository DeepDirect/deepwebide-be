package com.deepdirect.deepwebide_be.repository.dto.response;

import com.deepdirect.deepwebide_be.repository.domain.Repository;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SharedRepositoryResponse {

    private Long repositoryId;
    private String repositoryName;
    private Long ownerId;
    private String ownerName;
    private boolean isShared;
    private String shareLink;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isFavorite;

    public static SharedRepositoryResponse from(Repository repo) {
        return SharedRepositoryResponse.builder()
                .repositoryId(repo.getId())
                .repositoryName(repo.getRepositoryName())
                .ownerId(repo.getOwner().getId())
                .ownerName(repo.getOwner().getNickname())
                .isShared(repo.isShared())
                .shareLink(repo.getShareLink())
                .createdAt(repo.getCreatedAt())
                .updatedAt(repo.getUpdatedAt())
                .isFavorite(false)
                .build();
    }
}
