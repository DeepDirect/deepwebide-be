package com.deepdirect.deepwebide_be.repository.service;


import com.deepdirect.deepwebide_be.global.exception.ErrorCode;
import com.deepdirect.deepwebide_be.global.exception.GlobalException;
import com.deepdirect.deepwebide_be.member.domain.User;
import com.deepdirect.deepwebide_be.member.repository.UserRepository;
import com.deepdirect.deepwebide_be.repository.domain.*;
import com.deepdirect.deepwebide_be.repository.dto.request.RepositoryCreateRequest;
import com.deepdirect.deepwebide_be.repository.dto.request.RepositoryRenameRequest;
import com.deepdirect.deepwebide_be.repository.dto.response.*;
import com.deepdirect.deepwebide_be.repository.repository.*;
import com.deepdirect.deepwebide_be.repository.util.EntryCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class RepositoryService {

    private final RepositoryRepository repositoryRepository;
    private final UserRepository userRepository;
    private final RepositoryEntryCodeRepository entryCodeRepository;
    private final RepositoryMemberRepository repositoryMemberRepository;
    private final RepositoryFavoriteRepository repositoryFavoriteRepository;
    private final RepositoryFileService repositoryFileService;
    private final PortRegistryRepository portRegistryRepository;

    @Transactional
    public RepositoryCreateResponse createRepository(RepositoryCreateRequest request, Long ownerId) {
        User owner = getUserOrThrow(ownerId);

        if (repositoryRepository.existsByRepositoryNameAndOwnerIdAndDeletedAtIsNull(request.getRepositoryName(), ownerId)) {
            throw new GlobalException(ErrorCode.REPOSITORY_NAME_ALREADY_EXISTS);
        }

        Repository repository = Repository.builder()
                .repositoryName(request.getRepositoryName())
                .repositoryType(request.getRepositoryType())
                .owner(owner)
                .build();

        Repository savedRepository = repositoryRepository.save(repository);

        PortRegistry availablePort = portRegistryRepository
                .findFirstByStatusOrderByPortAsc(PortStatus.AVAILABLE)
                .orElseThrow(() -> new GlobalException(
                        ErrorCode.NO_AVAILABLE_PORT
                ));
        availablePort.setStatus(PortStatus.IN_USE);
        availablePort.setRepository(savedRepository);
        portRegistryRepository.save(availablePort);

        RepositoryMember ownerMember = RepositoryMember.builder()
                .repository(savedRepository)
                .user(owner)
                .role(RepositoryMemberRole.OWNER)
                .build();
        repositoryMemberRepository.save(ownerMember);

        // **S3 템플릿 zip → 압축 해제 → DB 파일/폴더 구조 저장**
        try {
            repositoryFileService.initializeTemplateFiles(savedRepository);
        } catch (Exception e) {
            throw new GlobalException(ErrorCode.TEMPLATE_DOWNLOAD_FAILED);
        }

        return RepositoryCreateResponse.builder()
                .repositoryId(savedRepository.getId())
                .repositoryName(savedRepository.getRepositoryName())
                .ownerId(owner.getId())
                .ownerName(owner.getNickname())
                .createdAt(savedRepository.getCreatedAt())
                .build();
    }

    public RepositoryListResponse getSharedRepositories(Long userId, Pageable pageable, Boolean liked) {
        Pageable sortedPageable = getSortedPageable(pageable);

        Page<Repository> repositoryPage = repositoryRepository
                .findByIsSharedTrueAndDeletedAtIsNullAndOwnerId(userId, sortedPageable);

        List<Repository> filtered = Boolean.TRUE.equals(liked)
                ? repositoryPage.stream()
                .filter(repo -> isFavoriteByUser(repo, userId))
                .toList()
                : repositoryPage.getContent();

        List<RepositoryResponse> sharedRepositoryDtos = filtered.stream()
                .map(repo -> RepositoryResponse.from(repo, isFavoriteByUser(repo, userId)))
                .toList();

        return RepositoryListResponse.builder()
                .currentPage(repositoryPage.getNumber())
                .pageSize(repositoryPage.getSize())
                .totalPages(repositoryPage.getTotalPages())
                .totalElements(repositoryPage.getTotalElements())
                .repositories(sharedRepositoryDtos)
                .build();
    }

    public RepositoryListResponse getReceivedSharedRepositories(Long userId, Pageable pageable, Boolean liked) {
        Pageable sortedPageable = getSortedPageable(pageable);

        Page<Repository> repositoryPage = repositoryRepository
                .findByMembersUserIdAndMembersRoleAndIsSharedTrueAndDeletedAtIsNullAndMembersDeletedAtIsNull(
                        userId, RepositoryMemberRole.MEMBER, sortedPageable);

        List<Repository> filtered = Boolean.TRUE.equals(liked)
                ? repositoryPage.stream()
                .filter(repo -> isFavoriteByUser(repo, userId))
                .toList()
                : repositoryPage.getContent();

        List<RepositoryResponse> sharedRepositoryDtos = filtered.stream()
                .map(repo -> RepositoryResponse.from(repo, isFavoriteByUser(repo, userId)))
                .toList();

        return RepositoryListResponse.builder()
                .currentPage(repositoryPage.getNumber())
                .pageSize(repositoryPage.getSize())
                .totalPages(repositoryPage.getTotalPages())
                .totalElements(repositoryPage.getTotalElements())
                .repositories(sharedRepositoryDtos)
                .build();
    }

    public RepositoryListResponse getMyRepositories(Long userId, Pageable pageable, Boolean liked) {
        Pageable sortedPageable = getSortedPageable(pageable);

        Page<Repository> repositoryPage = repositoryRepository
                .findByOwnerIdAndIsSharedFalseAndDeletedAtIsNull(userId, sortedPageable);

        List<Repository> filtered = Boolean.TRUE.equals(liked)
                ? repositoryPage.stream()
                .filter(repo -> isFavoriteByUser(repo, userId))
                .toList()
                : repositoryPage.getContent();

        List<RepositoryResponse> sharedRepositoryDtos = filtered.stream()
                .map(repo -> RepositoryResponse.from(repo, isFavoriteByUser(repo, userId)))
                .toList();

        return RepositoryListResponse.builder()
                .currentPage(repositoryPage.getNumber())
                .pageSize(repositoryPage.getSize())
                .totalPages(repositoryPage.getTotalPages())
                .totalElements(repositoryPage.getTotalElements())
                .repositories(sharedRepositoryDtos)
                .build();
    }

    @Transactional
    public RepositoryRenameResponse renameRepository(Long repoId, Long userId, RepositoryRenameRequest req) {
        Repository repo = repositoryRepository.findByIdAndDeletedAtIsNull(repoId)
                .orElseThrow(() -> new GlobalException(ErrorCode.REPOSITORY_NOT_FOUND));

        if (!repo.getOwner().getId().equals(userId)) {
            throw new GlobalException(ErrorCode.NOT_OWNER_CHANGE);
        }

        String newName = req.getRepositoryName();
        if (repositoryRepository.existsByRepositoryNameAndOwnerIdAndDeletedAtIsNull(newName, userId)) {
            throw new GlobalException(ErrorCode.REPOSITORY_NAME_ALREADY_EXISTS);
        }

        repo.updateRepositoryName(newName);
        repositoryRepository.save(repo);

        return RepositoryRenameResponse.builder()
                .repositoryId(repo.getId())
                .repositoryName(repo.getRepositoryName())
                .ownerId(userId)
                .ownerName(repo.getOwner().getNickname())
                .createdAt(repo.getCreatedAt())
                .updatedAt(repo.getUpdatedAt())
                .build();
    }
    @Transactional
    public RepositoryResponse toggleShareStatus(Long repositoryId, Long userId) {
        Repository repo = repositoryRepository.findByIdAndDeletedAtIsNull(repositoryId)
                .orElseThrow(() -> new GlobalException(ErrorCode.REPOSITORY_NOT_FOUND));

        if (!repo.getOwner().getId().equals(userId)) {
            if (repo.isShared()) {
                throw new GlobalException(ErrorCode.NOT_OWNER_TO_UNSHARE);
            } else {
                throw new GlobalException(ErrorCode.NOT_OWNER_TO_SHARE);
            }
        }

        boolean willShare = !repo.isShared();
        repo.updateSharedStatus(willShare);

        if (willShare) {
            repo.setShareLink("https://www.deepdirect.site/" + repositoryId);

            entryCodeRepository.findByRepositoryId(repositoryId).ifPresentOrElse(
                    entry -> {
                        entry.updateEntryCode(
                                EntryCodeGenerator.generateUniqueCode(entryCodeRepository::existsByEntryCode),
                                LocalDateTime.now().plusDays(3)
                        );
                    },
                    () -> {
                        RepositoryEntryCode newCode = RepositoryEntryCode.builder()
                                .repository(repo)
                                .entryCode(EntryCodeGenerator.generateUniqueCode(entryCodeRepository::existsByEntryCode))
                                .expiresAt(LocalDateTime.now().plusDays(3))
                                .build();
                        entryCodeRepository.save(newCode);
                    }
            );

        } else {
            // 공유 취소 시: 연결된 모든 멤버 soft delete (본인 제외)
            repositoryMemberRepository.findAllByRepositoryIdAndDeletedAtIsNull(repositoryId).stream()
                    .filter(member -> !member.getUser().getId().equals(userId))
                    .forEach(RepositoryMember::softDelete);

            // 링크 및 엔트리코드 삭제
            repo.setShareLink(null);
            entryCodeRepository.deleteByRepositoryId(repositoryId);
        }

        boolean isFavorite = repositoryFavoriteRepository
                .findByUserAndRepository(userRepository.getReferenceById(userId), repo)
                .isPresent();

        return RepositoryResponse.from(repo, isFavorite);
    }

    @Transactional
    public void deleteRepository(Long repositoryId, Long userId) {
        Repository repo = repositoryRepository.findByIdAndDeletedAtIsNull(repositoryId)
                .orElseThrow(() -> new GlobalException(ErrorCode.REPOSITORY_NOT_FOUND));

        if (!repo.getOwner().getId().equals(userId)) {
            throw new GlobalException(ErrorCode.NOT_OWNER_DELETE);
        }

        if (repo.isShared()) {
            throw new GlobalException(ErrorCode.CANNOT_DELETE_SHARED_REPOSITORY);
        }

        repositoryMemberRepository
                .findByRepositoryIdAndUserIdAndDeletedAtIsNull(repositoryId, userId)
                .ifPresent(RepositoryMember::softDelete);

        repo.softDelete();
    }

    @Transactional
    public void exitSharedRepository(Long repositoryId, Long userId) {
        Repository repo = repositoryRepository.findByIdAndDeletedAtIsNull(repositoryId)
                .orElseThrow(() -> new GlobalException(ErrorCode.REPOSITORY_NOT_FOUND));

        RepositoryMember member = repositoryMemberRepository
                .findByRepositoryIdAndUserIdAndDeletedAtIsNull(repositoryId, userId)
                .orElseThrow(() -> new GlobalException(ErrorCode.NOT_MEMBER));


        member.softDelete(); // 💡 논리 삭제 수행

        if (repo.isShared()) {
            entryCodeRepository.findByRepositoryId(repositoryId).ifPresent(entry -> {
                entry.updateEntryCode(
                        EntryCodeGenerator.generateUniqueCode(entryCodeRepository::existsByEntryCode),
                        LocalDateTime.now().plusDays(3)
                );
            });
        }
    }

    @Transactional
    public KickedMemberResponse kickMember(Long repositoryId, Long ownerId, Long memberId) {
        Repository repo = repositoryRepository.findByIdAndDeletedAtIsNull(repositoryId)
                .orElseThrow(() -> new GlobalException(ErrorCode.REPOSITORY_NOT_FOUND));

        if (!repo.getOwner().getId().equals(ownerId)) {
            throw new GlobalException(ErrorCode.NOT_OWNER_TO_KICK);
        }

        if (ownerId.equals(memberId)) {
            throw new GlobalException(ErrorCode.CANNOT_KICK_SELF);
        }

        RepositoryMember member = repositoryMemberRepository
                .findByRepositoryIdAndUserIdAndDeletedAtIsNull(repositoryId, memberId)
                .orElseThrow(() -> new GlobalException(ErrorCode.NOT_MEMBER));

        member.softDelete();

        RepositoryEntryCode entryCode = entryCodeRepository.findByRepositoryId(repositoryId)
                .orElseThrow(() -> new GlobalException(ErrorCode.ENTRY_CODE_NOT_FOUND));

        String newCode = EntryCodeGenerator.generateUniqueCode(entryCodeRepository::existsByEntryCode);
        entryCode.updateEntryCode(newCode, LocalDateTime.now().plusDays(3));

        return new KickedMemberResponse(memberId);
    }

    @Transactional(readOnly = true)
    public RepositorySettingResponse getRepositorySettings(Long repositoryId, Long userId) {
        Repository repository = repositoryRepository.findByIdAndDeletedAtIsNull(repositoryId)
                .orElseThrow(() -> new GlobalException(ErrorCode.REPOSITORY_NOT_FOUND));

        boolean isOwner = repository.getOwner().getId().equals(userId);
        boolean isMember = repositoryMemberRepository.existsByRepositoryIdAndUserIdAndDeletedAtIsNull(repositoryId, userId);

        // 공유 여부와 무관하게 접근 권한 없는 경우 차단
        if (!isOwner && !isMember) {
            throw new GlobalException(ErrorCode.FORBIDDEN);
        }

        boolean isShared = repository.isShared();
        List<RepositorySettingResponse.MemberInfo> memberInfos = new ArrayList<>();

        if (isShared) {
            List<RepositoryMember> members = repositoryMemberRepository.findAllByRepositoryIdAndDeletedAtIsNull(repositoryId);
            for (RepositoryMember member : members) {
                User user = member.getUser();
                memberInfos.add(RepositorySettingResponse.MemberInfo.builder()
                        .userId(user.getId())
                        .nickname(user.getNickname())
                        .profileImageUrl(user.getProfileImageUrl())
                        .role(member.getRole())
                        .build());
            }
        }

        return RepositorySettingResponse.builder()
                .repositoryId(repository.getId())
                .repositoryName(repository.getRepositoryName())
                .createdAt(repository.getCreatedAt())
                .updatedAt(repository.getUpdatedAt())
                .IsShared(isShared)
                .shareLink(repository.getShareLink())
                .members(memberInfos)
                .build();
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));
    }

    private PageRequest getSortedPageable(Pageable pageable) {
        return PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize()
        );
    }

    private List<RepositoryResponse> convertToListRepo(Page<Repository> repositoryPage, Long userId) {
        return repositoryPage.stream()
                .map(repo -> RepositoryResponse.from(repo, isFavoriteByUser(repo, userId)))
                .collect(Collectors.toList());
    }

    private boolean isFavoriteByUser(Repository repo, Long userId) {
        return repo.getFavorites().stream()
                .anyMatch(fav -> fav.getUser().getId().equals(userId));
    }
}

