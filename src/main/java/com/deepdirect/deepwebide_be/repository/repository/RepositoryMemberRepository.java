package com.deepdirect.deepwebide_be.repository.repository;


import com.deepdirect.deepwebide_be.repository.domain.RepositoryMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;

@Repository
public interface RepositoryMemberRepository extends JpaRepository<RepositoryMember, Long> {

    // 조회
    Optional<RepositoryMember> findByRepositoryIdAndUserIdAndDeletedAtIsNull(Long repositoryId, Long userId);
    Optional<RepositoryMember> findByRepositoryIdAndUserIdAndDeletedAtIsNotNull(Long repositoryId, Long userId);
    List<RepositoryMember> findAllByRepositoryId(Long repositoryId);

    // 존재 확인
    boolean existsByRepositoryIdAndUserIdAndDeletedAtIsNull(Long repositoryId, Long userId);

    // 카운트
    long countByRepositoryIdAndDeletedAtIsNull(Long repositoryId);

}