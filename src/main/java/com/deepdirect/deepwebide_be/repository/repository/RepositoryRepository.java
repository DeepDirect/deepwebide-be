package com.deepdirect.deepwebide_be.repository.repository;

import com.deepdirect.deepwebide_be.repository.domain.Repository;
import com.deepdirect.deepwebide_be.repository.domain.RepositoryMemberRole;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;


@org.springframework.stereotype.Repository
public interface RepositoryRepository extends JpaRepository<Repository, Long> {

    // 조회 (소유자 기준)
    Page<Repository> findByOwnerIdAndIsSharedFalseAndDeletedAtIsNull(Long userId, Pageable sortedPageable);
    Page<Repository> findByIsSharedTrueAndDeletedAtIsNullAndOwnerId(Long ownerId, Pageable sortedPageable);

    // 조회 (참여자 기준)
    Page<Repository> findByMembersUserIdAndMembersRoleAndIsSharedTrueAndDeletedAtIsNullAndMembersDeletedAtIsNull(Long userId, RepositoryMemberRole role, Pageable pageable);

    // 존재 확인
    boolean existsByRepositoryNameAndOwnerIdAndDeletedAtIsNull(String repositoryName, Long ownerId);


    @Query("""
    SELECT r FROM Repository r
    LEFT JOIN r.members m
    WHERE r.id = :repositoryId
    AND (r.owner.id = :userId OR (m.user.id = :userId AND m.deletedAt IS NULL))
    AND r.deletedAt IS NULL
    """)
    Optional<Repository> findByIdAndMemberOrOwner(Long repositoryId, Long userId);
}
