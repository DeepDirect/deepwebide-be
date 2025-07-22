package com.deepdirect.deepwebide_be.repository.repository;

import com.deepdirect.deepwebide_be.repository.domain.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;


@org.springframework.stereotype.Repository
public interface RepositoryRepository extends JpaRepository<Repository, Long> {

    boolean existsByRepositoryNameAndOwnerIdAndDeletedAtIsNull(String repositoryName, Long ownerId);
    Page<Repository> findByIsSharedTrueAndDeletedAtIsNull(Pageable pageable);
}
