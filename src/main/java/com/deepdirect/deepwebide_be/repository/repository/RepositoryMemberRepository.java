package com.deepdirect.deepwebide_be.repository.repository;


import com.deepdirect.deepwebide_be.repository.domain.RepositoryMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepositoryMemberRepository extends JpaRepository<RepositoryMember, Long> {

    boolean existsByRepositoryIdAndUserId(Long repositoryId, Long userId);
    long countByRepositoryIdAndDeletedAtIsNull(Long repositoryId);
    void deleteByUserIdAndRepositoryId(Long userId, Long repositoryId);

    List<RepositoryMember> findAllByRepositoryId(Long repositoryId);
}