package com.deepdirect.deepwebide_be.repository.repository;

import com.deepdirect.deepwebide_be.repository.domain.RunningContainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RunningContainerRepository extends JpaRepository<RunningContainer, Long> {

    Optional<RunningContainer> findByRepositoryId(Long repositoryId);

    @Modifying
    @Query("DELETE FROM RunningContainer rc WHERE rc.repositoryId = :repositoryId")
    void deleteByRepositoryId(@Param("repositoryId") Long repositoryId);

    List<RunningContainer> findByStatus(String status);

    Optional<RunningContainer> findByUuid(String uuid);

    @Query("SELECT rc FROM RunningContainer rc WHERE rc.status = 'RUNNING' AND rc.createdAt < :before")
    List<RunningContainer> findStaleContainers(@Param("before") LocalDateTime before);

    @Query("SELECT COUNT(rc) FROM RunningContainer rc WHERE rc.status = 'RUNNING'")
    long countRunningContainers();

    List<RunningContainer> findAllByStatusAndCreatedAtBefore(String status, LocalDateTime dateTime);

}
