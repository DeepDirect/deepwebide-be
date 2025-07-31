package com.deepdirect.deepwebide_be.file.repository;

import com.deepdirect.deepwebide_be.file.domain.FileContent;
import com.deepdirect.deepwebide_be.file.domain.FileNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FileContentRepository extends JpaRepository<FileContent, Long> {

    void deleteByFileNode(FileNode fileNode);

    Optional<FileContent> findByFileNode(FileNode fileNode);

    @Query("SELECT fc FROM FileContent fc WHERE fc.fileNode.repository.id = :repositoryId")
    List<FileContent> findAllByRepositoryId(@Param("repositoryId") Long repositoryId);
}
