package com.deepdirect.deepwebide_be.repository.service;

import com.deepdirect.deepwebide_be.global.exception.ErrorCode;
import com.deepdirect.deepwebide_be.global.exception.GlobalException;
import com.deepdirect.deepwebide_be.member.domain.User;
import com.deepdirect.deepwebide_be.member.repository.UserRepository;
import com.deepdirect.deepwebide_be.repository.domain.Repository;
import com.deepdirect.deepwebide_be.repository.domain.RepositoryEntryCode;
import com.deepdirect.deepwebide_be.repository.domain.RepositoryMember;
import com.deepdirect.deepwebide_be.repository.domain.RepositoryMemberRole;
import com.deepdirect.deepwebide_be.repository.dto.response.RepositoryAccessCheckResponse;
import com.deepdirect.deepwebide_be.repository.dto.response.RepositoryEntryCodeResponse;
import com.deepdirect.deepwebide_be.repository.dto.response.RepositoryJoinResponse;
import com.deepdirect.deepwebide_be.repository.dto.response.RepositorySummary;
import com.deepdirect.deepwebide_be.repository.repository.RepositoryEntryCodeRepository;
import com.deepdirect.deepwebide_be.repository.repository.RepositoryMemberRepository;
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
    private final RepositoryMemberRepository repositoryMemberRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public RepositoryAccessCheckResponse checkAccess(Long repositoryId, Long userId) {
        Repository repository = repositoryRepository.findById(repositoryId)
                .orElseThrow(() -> new GlobalException(ErrorCode.REPOSITORY_NOT_FOUND));

        boolean isMember = repositoryMemberRepository.existsByRepositoryIdAndUserId(repositoryId, userId);
        if (!repository.isShared() || !isMember) {
            return RepositoryAccessCheckResponse.builder()
                    .access(false)
                    .build();
        }

        RepositorySummary summary = RepositorySummary.builder()
                .repositoryId(repository.getId())
                .repositoryName(repository.getRepositoryName())
                .ownerId(repository.getOwner().getId())
                .ownerName(repository.getOwner().getNickname())
                .isShared(repository.isShared())
                .shareLink(repository.getShareLink())
                .createdAt(repository.getCreatedAt())
                .updatedAt(repository.getUpdatedAt())
                .build();

        return RepositoryAccessCheckResponse.builder()
                .access(true)
                .repository(summary)
                .build();
    }


    @Transactional
    public RepositoryJoinResponse verifyEntryCodeAndJoin(Long repositoryId, String entryCode, Long userId) {
        Repository repo = repositoryRepository.findById(repositoryId)
                .orElseThrow(() -> new GlobalException(ErrorCode.REPOSITORY_NOT_FOUND));

        if (!repo.isShared()) {
            throw new GlobalException(ErrorCode.REPOSITORY_NOT_SHARED);
        }

        RepositoryEntryCode entryCodeEntity = entryCodeRepository
                .findByRepositoryIdAndExpiresAtAfter(repositoryId, LocalDateTime.now())
                .orElseThrow(() -> new GlobalException(ErrorCode.ENTRY_CODE_NOT_FOUND));

        if (!entryCodeEntity.getEntryCode().equals(entryCode)) {
            throw new GlobalException(ErrorCode.INVALID_ENTRY_CODE);
        }

        boolean alreadyJoined = repositoryMemberRepository.existsByRepositoryIdAndUserId(repositoryId, userId);
        if (alreadyJoined) {
            throw new GlobalException(ErrorCode.ALREADY_JOINED);
        }

        long activeMemberCount = repositoryMemberRepository.countByRepositoryIdAndDeletedAtIsNull(repositoryId);
        if (activeMemberCount >= 4) {
            throw new GlobalException(ErrorCode.REPOSITORY_MEMBER_LIMIT_EXCEEDED);
        }


        User user = userRepository.getReferenceById(userId);


        // 참여자 등록
        RepositoryMember member = RepositoryMember.builder()
                .repository(repo)
                .user(user)
                .role(RepositoryMemberRole.MEMBER)
                .build();
        repositoryMemberRepository.save(member);

        return RepositoryJoinResponse.builder()
                .repositoryId(repo.getId())
                .repositoryName(repo.getRepositoryName())
                .ownerId(repo.getOwner().getId())
                .ownerName(repo.getOwner().getUsername())
                .isShared(repo.isShared())
                .shareLink(repo.getShareLink())
                .createdAt(repo.getCreatedAt())
                .updatedAt(repo.getUpdatedAt())
                .build();
    }

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
                .isShared(repo.isShared())
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

