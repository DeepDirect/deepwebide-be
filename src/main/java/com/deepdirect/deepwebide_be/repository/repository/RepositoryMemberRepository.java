package com.deepdirect.deepwebide_be.repository.repository;


import com.deepdirect.deepwebide_be.repository.domain.RepositoryMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepositoryMemberRepository extends JpaRepository<RepositoryMember, Long> {

    boolean existsByRepositoryIdAndUserId(Long repositoryId, Long userId);
    boolean existsByUserIdAndRepositoryId(Long userId, Long repositoryId);

    long countByRepositoryIdAndDeletedAtIsNull(Long repositoryId);
    void deleteByUserIdAndRepositoryId(Long userId, Long repositoryId);
}