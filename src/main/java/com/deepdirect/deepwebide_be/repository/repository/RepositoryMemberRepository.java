package com.deepdirect.deepwebide_be.repository.repository;


import com.deepdirect.deepwebide_be.repository.domain.RepositoryMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.Optional;

@Repository
public interface RepositoryMemberRepository extends JpaRepository<RepositoryMember, Long> {

    boolean existsByRepositoryIdAndUserIdAndDeletedAtIsNull(Long repositoryId, Long userId);
    long countByRepositoryIdAndDeletedAtIsNull(Long repositoryId);
    Optional<RepositoryMember> findByRepositoryIdAndUserIdAndDeletedAtIsNull(Long repositoryId, Long userId);
    Optional<RepositoryMember> findByRepositoryIdAndUserIdAndDeletedAtIsNotNull(Long repositoryId, Long userId);

    void deleteByUserIdAndRepositoryId(Long userId, Long repositoryId);

    List<RepositoryMember> findAllByRepositoryId(Long repositoryId);

}