package com.deepdirect.deepwebide_be.repository.service;

import com.deepdirect.deepwebide_be.global.exception.ErrorCode;
import com.deepdirect.deepwebide_be.global.exception.GlobalException;
import com.deepdirect.deepwebide_be.repository.domain.Repository;
import com.deepdirect.deepwebide_be.repository.domain.RepositoryEntryCode;
import com.deepdirect.deepwebide_be.repository.dto.response.RepositoryEntryCodeResponse;
import com.deepdirect.deepwebide_be.repository.repository.RepositoryEntryCodeRepository;
import com.deepdirect.deepwebide_be.repository.repository.RepositoryRepository;
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

    private static final int ENTRY_CODE_LENGTH = 8;
    private static final int MAX_TRY_COUNT = 10;

    @Transactional(readOnly = true)
    public RepositoryEntryCodeResponse getEntryCode(Long repositoryId, Long userId) {

        Repository repo = repositoryRepository.findById(repositoryId)
                .orElseThrow(() -> new GlobalException(ErrorCode.REPOSITORY_NOT_FOUND));

        if (!repo.getOwner().getId().equals(userId)) {
            throw new GlobalException(ErrorCode.ENTRY_CODE_ACCESS_DENIED);
        }

        if (!repo.isShared()) {
            throw new GlobalException(ErrorCode.REPOSITORY_NOT_SHARED);
        }

        RepositoryEntryCode entryCode = entryCodeRepository.findByRepositoryIdAndExpiresAtAfter(repo.getId(), LocalDateTime.now())
                .orElseGet(() -> createNewEntryCode(repo));

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

        RepositoryEntryCode newCode = createNewEntryCode(repo);
        return newCode.getEntryCode();
    }



    private RepositoryEntryCode createNewEntryCode(Repository repo) {
        String code = generateUniqueEntryCode();

        RepositoryEntryCode entryCode = RepositoryEntryCode.builder()
                .repository(repo)
                .entryCode(code)
                .expiresAt(LocalDateTime.now().plusDays(3))
                .build();

        return entryCodeRepository.save(entryCode);
    }

    private String generateUniqueEntryCode() {
        for (int i = 0; i < MAX_TRY_COUNT; i++) {
            String code = generateRandomCode(ENTRY_CODE_LENGTH);
            boolean exists = entryCodeRepository.existsByEntryCode(code);
            if (!exists) return code;
        }
        throw new GlobalException(ErrorCode.ENTRY_CODE_GENERATION_FAILED);
    }

    private String generateRandomCode(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(length);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}

