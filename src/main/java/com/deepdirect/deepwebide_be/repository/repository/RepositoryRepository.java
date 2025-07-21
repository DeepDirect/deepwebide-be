package com.deepdirect.deepwebide_be.repository.repository;

import com.deepdirect.deepwebide_be.repository.domain.Repository;
import org.springframework.data.jpa.repository.JpaRepository;


@org.springframework.stereotype.Repository
public interface RepositoryRepository extends JpaRepository<Repository, Long> {

    boolean existsByRepositoryNameAndOwnerIdAndDeletedAtIsNull(String repositoryName, Long ownerId);
}
