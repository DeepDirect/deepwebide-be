package com.deepdirect.deepwebide_be.repository.service;

import com.deepdirect.deepwebide_be.global.exception.ErrorCode;
import com.deepdirect.deepwebide_be.global.exception.GlobalException;
import com.deepdirect.deepwebide_be.repository.domain.Repository;
import com.deepdirect.deepwebide_be.repository.domain.RepositoryEntryCode;
import com.deepdirect.deepwebide_be.repository.dto.response.RepositoryEntryCodeResponse;
import com.deepdirect.deepwebide_be.repository.repository.RepositoryEntryCodeRepository;
import com.deepdirect.deepwebide_be.repository.repository.RepositoryRepository;
import com.deepdirect.deepwebide_be.repository.util.EntryCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class RepositoryEntryCodeService {

    private final RepositoryRepository repositoryRepository;
    private final RepositoryEntryCodeRepository entryCodeRepository;

    @Transactional
    public RepositoryEntryCodeResponse getEntryCode(Long repositoryId, Long userId) {

        Repository repo = repositoryRepository.findById(repositoryId)
                .orElseThrow(() -> new GlobalException(ErrorCode.REPOSITORY_NOT_FOUND));

        if (!repo.getOwner().getId().equals(userId)) {
            throw new GlobalException(ErrorCode.ENTRY_CODE_ACCESS_DENIED);
        }

        if (!repo.isShared()) {
            throw new GlobalException(ErrorCode.REPOSITORY_NOT_SHARED);
        }

        RepositoryEntryCode entryCode = entryCodeRepository
                .findByRepositoryIdAndExpiresAtAfter(repo.getId(), LocalDateTime.now())
                .orElseGet(() -> regenerateEntryCodeInternal(repo));

        return RepositoryEntryCodeResponse.builder()
                .repositoryId(repo.getId())
                .repositoryName(repo.getRepositoryName())
                .ownerId(repo.getOwner().getId())
                .ownerName(repo.getOwner().getUsername())
                .isShared(true)
                .shareLink(repo.getShareLink())
                .entryCode(entryCode.getEntryCode())
                .createdAt(repo.getCreatedAt())
                .updatedAt(repo.getUpdatedAt())
                .build();
    }

    @Transactional
    public String regenerateEntryCode(Long repositoryId, Long userId) {
        Repository repo = repositoryRepository.findById(repositoryId)
                .orElseThrow(() -> new GlobalException(ErrorCode.REPOSITORY_NOT_FOUND));

        if (!repo.getOwner().getId().equals(userId)) {
            throw new GlobalException(ErrorCode.ENTRY_CODE_REISSUE_DENIED);
        }

        if (!repo.isShared()) {
            throw new GlobalException(ErrorCode.REPOSITORY_NOT_SHARED);
        }

        return regenerateEntryCodeInternal(repo).getEntryCode();
    }

    /**
     * 내부에서 엔트리 코드를 재생성하고 업데이트하는 로직
     */
    private RepositoryEntryCode regenerateEntryCodeInternal(Repository repo) {
        RepositoryEntryCode entryCode = entryCodeRepository.findByRepositoryId(repo.getId())
                .orElseThrow(() -> new GlobalException(ErrorCode.ENTRY_CODE_NOT_FOUND));

        String newCode = EntryCodeGenerator.generateUniqueCode(entryCodeRepository::existsByEntryCode);
        entryCode.updateEntryCode(newCode, LocalDateTime.now().plusDays(3));
        return entryCode;
    }

}

