package com.deepdirect.deepwebide_be.repository.repository;

import com.deepdirect.deepwebide_be.repository.domain.Repository;
import com.deepdirect.deepwebide_be.repository.domain.RepositoryMemberRole;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;


@org.springframework.stereotype.Repository
public interface RepositoryRepository extends JpaRepository<Repository, Long> {

    // 조회 (소유자 기준)
    Page<Repository> findByOwnerIdAndDeletedAtIsNull(Long ownerId, Pageable pageable);
    Page<Repository> findByIsSharedTrueAndDeletedAtIsNullAndOwnerId(Long ownerId, Pageable pageable);

    // 조회 (참여자 기준)
    Page<Repository> findByMembersUserIdAndMembersRoleAndIsSharedTrueAndDeletedAtIsNullAndMembersDeletedAtIsNull(Long userId, RepositoryMemberRole role, Pageable pageable);

    // 존재 확인
    boolean existsByRepositoryNameAndOwnerIdAndDeletedAtIsNull(String repositoryName, Long ownerId);
}
