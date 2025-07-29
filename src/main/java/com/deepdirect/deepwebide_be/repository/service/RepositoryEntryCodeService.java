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

@Service
@RequiredArgsConstructor
public class RepositoryEntryCodeService {

    private final RepositoryRepository repositoryRepository;
    private final RepositoryEntryCodeRepository entryCodeRepository;
    private final RepositoryMemberRepository repositoryMemberRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public RepositoryAccessCheckResponse checkAccess(Long repositoryId, Long userId) {
        Repository repository = getRepositoryOrThrow(repositoryId);

        Optional<RepositoryMember> optionalMember =
                repositoryMemberRepository.findByRepositoryIdAndUserIdAndDeletedAtIsNull(repositoryId, userId);

        RepositorySummary summary = createRepositorySummary(repository);

        boolean access = optionalMember.isPresent();

        return RepositoryAccessCheckResponse.builder()
                .access(access)
                .repository(summary)
                .build();
    }


    @Transactional
    public RepositoryJoinResponse verifyEntryCodeAndJoin(Long repositoryId, String entryCode, Long userId) {
        Repository repo = getRepositoryOrThrow(repositoryId);

        if (!repo.isShared()) {
            throw new GlobalException(ErrorCode.REPOSITORY_NOT_SHARED);
        }

        RepositoryEntryCode entryCodeEntity = entryCodeRepository
                .findByRepositoryIdAndExpiresAtAfter(repositoryId, LocalDateTime.now())
                .orElseThrow(() -> new GlobalException(ErrorCode.ENTRY_CODE_NOT_FOUND));

        if (!entryCodeEntity.getEntryCode().equals(entryCode)) {
            throw new GlobalException(ErrorCode.INVALID_ENTRY_CODE);
        }

        long activeMemberCount = repositoryMemberRepository.countByRepositoryIdAndDeletedAtIsNull(repositoryId);
        if (activeMemberCount >= 4) {
            throw new GlobalException(ErrorCode.REPOSITORY_MEMBER_LIMIT_EXCEEDED);
        }

        User user = userRepository.getReferenceById(userId);

        // 항상 새로운 참여자 생성(중복 여부는 이전에 검증)
        RepositoryMember newMember = RepositoryMember.builder()
                .repository(repo)
                .user(user)
                .role(RepositoryMemberRole.MEMBER)
                .build();
        repositoryMemberRepository.save(newMember);

        return RepositoryJoinResponse.builder()
                .repositoryId(repo.getId())
                .repositoryName(repo.getRepositoryName())
                .ownerId(repo.getOwner().getId())
                .ownerName(repo.getOwner().getNickname())
                .IsShared(repo.isShared())
                .shareLink(repo.getShareLink())
                .createdAt(repo.getCreatedAt())
                .updatedAt(repo.getUpdatedAt())
                .build();
    }

    @Transactional
    public RepositoryEntryCodeResponse getEntryCode(Long repositoryId, Long userId) {
        Repository repo = getRepositoryOrThrow(repositoryId);
        validateOwnerOrThrow(repo, userId,ErrorCode.ENTRY_CODE_ACCESS_DENIED);

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
                .ownerName(repo.getOwner().getNickname())
                .IsShared(repo.isShared())
                .shareLink(repo.getShareLink())
                .entryCode(entryCode.getEntryCode())
                .createdAt(repo.getCreatedAt())
                .updatedAt(repo.getUpdatedAt())
                .build();
    }

    @Transactional
    public String regenerateEntryCode(Long repositoryId, Long userId) {
        Repository repo = getRepositoryOrThrow(repositoryId);
        validateOwnerOrThrow(repo, userId,ErrorCode.ENTRY_CODE_REISSUE_DENIED);

        if (!repo.isShared()) {
            throw new GlobalException(ErrorCode.REPOSITORY_NOT_SHARED);
        }

        return regenerateEntryCodeInternal(repo).getEntryCode();
    }

    private Repository getRepositoryOrThrow(Long repositoryId) {
        return repositoryRepository.findById(repositoryId)
                .orElseThrow(() -> new GlobalException(ErrorCode.REPOSITORY_NOT_FOUND));
    }

    private void validateOwnerOrThrow(Repository repo, Long userId, ErrorCode errorCode) {
        if (!repo.getOwner().getId().equals(userId)) {
            throw new GlobalException(errorCode);
        }
    }

    private RepositorySummary createRepositorySummary(Repository repo) {
        return RepositorySummary.builder()
                .repositoryId(repo.getId())
                .repositoryName(repo.getRepositoryName())
                .ownerId(repo.getOwner().getId())
                .ownerName(repo.getOwner().getNickname())
                .IsShared(repo.isShared())
                .shareLink(repo.getShareLink())
                .createdAt(repo.getCreatedAt())
                .updatedAt(repo.getUpdatedAt())
                .build();
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

