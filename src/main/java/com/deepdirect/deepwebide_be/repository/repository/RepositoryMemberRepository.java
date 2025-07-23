package com.deepdirect.deepwebide_be.repository.repository;


import com.deepdirect.deepwebide_be.repository.domain.RepositoryMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface RepositoryMemberRepository extends JpaRepository<RepositoryMember, Long> {

    boolean existsByRepositoryIdAndUserId(Long repositoryId, Long userId);
    long countByRepositoryIdAndDeletedAtIsNull(Long repositoryId);
    void deleteByUserIdAndRepositoryId(Long userId, Long repositoryId);

    List<RepositoryMember> findAllByRepositoryId(Long repositoryId);
}