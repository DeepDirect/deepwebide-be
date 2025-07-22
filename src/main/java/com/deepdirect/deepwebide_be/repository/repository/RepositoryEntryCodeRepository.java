package com.deepdirect.deepwebide_be.repository.repository;

import com.deepdirect.deepwebide_be.repository.domain.RepositoryEntryCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RepositoryEntryCodeRepository extends JpaRepository<RepositoryEntryCode, Long> {

    Optional<RepositoryEntryCode> findByRepositoryIdAndExpiresAtAfter(Long repositoryId, LocalDateTime now);

    boolean existsByEntryCode(String entryCode);

    Optional<RepositoryEntryCode> findByRepositoryId(Long id);
}