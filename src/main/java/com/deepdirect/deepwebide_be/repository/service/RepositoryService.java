package com.deepdirect.deepwebide_be.repository.service;


import com.deepdirect.deepwebide_be.global.exception.ErrorCode;
import com.deepdirect.deepwebide_be.global.exception.GlobalException;
import com.deepdirect.deepwebide_be.member.domain.User;
import com.deepdirect.deepwebide_be.member.repository.UserRepository;
import com.deepdirect.deepwebide_be.repository.domain.Repository;
import com.deepdirect.deepwebide_be.repository.domain.RepositoryMemberRole;
import com.deepdirect.deepwebide_be.repository.dto.request.RepositoryCreateRequest;
import com.deepdirect.deepwebide_be.repository.dto.response.RepositoryCreateResponse;
import com.deepdirect.deepwebide_be.repository.dto.response.RepositoryResponse;
import com.deepdirect.deepwebide_be.repository.dto.response.RepositoryListResponse;
import com.deepdirect.deepwebide_be.repository.repository.RepositoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class RepositoryService {

    private final RepositoryRepository repositoryRepository;
    private final UserRepository userRepository;

    @Transactional
    public RepositoryCreateResponse createRepository(RepositoryCreateRequest request, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));

        if (repositoryRepository.existsByRepositoryNameAndOwnerIdAndDeletedAtIsNull(request.getRepositoryName(), ownerId)) {
            throw new GlobalException(ErrorCode.REPOSITORY_NAME_ALREADY_EXISTS);
        }

        Repository repository = Repository.builder()
                .repositoryName(request.getRepositoryName())
                .repositoryType(request.getRepositoryType())
                .owner(owner)
                .build();

        Repository savedRepository = repositoryRepository.save(repository);

        return RepositoryCreateResponse.builder()
                .repositoryId(savedRepository.getId())
                .repositoryName(savedRepository.getRepositoryName())
                .ownerId(owner.getId())
                .ownerName(owner.getUsername())
                .createdAt(savedRepository.getCreatedAt())
                .build();
    }

    public RepositoryListResponse getSharedRepositories(Long userId, Pageable pageable) {
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize()
        );

        Page<Repository> repositoryPage = repositoryRepository
                .findByIsSharedTrueAndDeletedAtIsNullAndOwnerId(userId, sortedPageable);

        List<RepositoryResponse> sharedRepositoryDtos = repositoryPage.stream()
                .map(RepositoryResponse::from)
                .collect(Collectors.toList());

        return RepositoryListResponse.builder()
                .currentPage(repositoryPage.getNumber())
                .pageSize(repositoryPage.getSize())
                .totalPages(repositoryPage.getTotalPages())
                .totalElements(repositoryPage.getTotalElements())
                .repositories(sharedRepositoryDtos)
                .build();
    }

    public RepositoryListResponse getReceivedSharedRepositories(Long userId, Pageable pageable) {
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize()
        );

        Page<Repository> repositoryPage = repositoryRepository
                .findByMembersUserIdAndMembersRoleAndIsSharedTrueAndDeletedAtIsNullAndMembersDeletedAtIsNull(
                        userId, RepositoryMemberRole.MEMBER, sortedPageable);

        List<RepositoryResponse> sharedRepositoryDtos = repositoryPage.stream()
                .map(RepositoryResponse::from)
                .collect(Collectors.toList());

        return RepositoryListResponse.builder()
                .currentPage(repositoryPage.getNumber())
                .pageSize(repositoryPage.getSize())
                .totalPages(repositoryPage.getTotalPages())
                .totalElements(repositoryPage.getTotalElements())
                .repositories(sharedRepositoryDtos)
                .build();
    }
    public RepositoryListResponse getMyRepositories(Long userId, Pageable pageable) {
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize()
        );

        Page<Repository> repositoryPage = repositoryRepository
                .findByOwnerIdAndDeletedAtIsNull(userId, sortedPageable);

        List<RepositoryResponse> sharedRepositoryDtos = repositoryPage.stream()
                .map(RepositoryResponse::from)
                .collect(Collectors.toList());

        return RepositoryListResponse.builder()
                .currentPage(repositoryPage.getNumber())
                .pageSize(repositoryPage.getSize())
                .totalPages(repositoryPage.getTotalPages())
                .totalElements(repositoryPage.getTotalElements())
                .repositories(sharedRepositoryDtos)
                .build();
    }
}
