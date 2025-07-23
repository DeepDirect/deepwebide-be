package com.deepdirect.deepwebide_be.repository.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RepositoryRenameResponse {
    private Long repositoryId;
    private String repositoryName;
    private Long ownerId;
    private String ownerName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}