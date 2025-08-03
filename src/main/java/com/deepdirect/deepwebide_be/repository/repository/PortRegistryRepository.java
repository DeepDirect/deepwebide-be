package com.deepdirect.deepwebide_be.repository.repository;

import com.deepdirect.deepwebide_be.repository.domain.PortRegistry;
import com.deepdirect.deepwebide_be.repository.domain.PortStatus;
import com.deepdirect.deepwebide_be.repository.domain.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PortRegistryRepository extends JpaRepository<PortRegistry, Long> {

    Optional<PortRegistry> findByRepository(Repository repository);
    Optional<PortRegistry> findFirstByStatus(PortStatus status);
    Optional<PortRegistry> findByRepositoryId(Long repositoryId);

}
