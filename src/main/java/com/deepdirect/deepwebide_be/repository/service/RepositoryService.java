package com.deepdirect.deepwebide_be.repository.service;


import com.deepdirect.deepwebide_be.global.exception.ErrorCode;
import com.deepdirect.deepwebide_be.global.exception.GlobalException;
import com.deepdirect.deepwebide_be.member.domain.User;
import com.deepdirect.deepwebide_be.member.repository.UserRepository;
import com.deepdirect.deepwebide_be.repository.domain.Repository;
import com.deepdirect.deepwebide_be.repository.dto.request.RepositoryCreateRequest;
import com.deepdirect.deepwebide_be.repository.dto.response.RepositoryCreateResponse;
import com.deepdirect.deepwebide_be.repository.dto.response.SharedRepositoryResponse;
import com.deepdirect.deepwebide_be.repository.dto.response.SharedRepositoryListResponse;
import com.deepdirect.deepwebide_be.repository.repository.RepositoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;
import java.util.List;

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

    public SharedRepositoryListResponse getSharedRepositories(Pageable pageable) {
        Page<Repository> sharedRepos = repositoryRepository.findByIsSharedTrueAndDeletedAtIsNull(pageable);

        List<SharedRepositoryResponse> repositoryDtos = sharedRepos.getContent().stream()
                .map(SharedRepositoryResponse::from)
                .toList();

        return SharedRepositoryListResponse.builder()
                .currentPage(sharedRepos.getNumber())
                .pageSize(sharedRepos.getSize())
                .totalPages(sharedRepos.getTotalPages())
                .totalElements(sharedRepos.getTotalElements())
                .repositories(repositoryDtos)
                .build();
    }
}
