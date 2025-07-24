package com.deepdirect.deepwebide_be.repository.repository;

import com.deepdirect.deepwebide_be.repository.domain.RepositoryEntryCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface RepositoryEntryCodeRepository extends JpaRepository<RepositoryEntryCode, Long> {

    // 조회
    Optional<RepositoryEntryCode> findByRepositoryId(Long id);
    Optional<RepositoryEntryCode> findByRepositoryIdAndExpiresAtAfter(Long repositoryId, LocalDateTime now);

    // 존재 확인
    boolean existsByEntryCode(String entryCode);

    // 삭제
    void deleteByRepositoryId(Long repositoryId);
}