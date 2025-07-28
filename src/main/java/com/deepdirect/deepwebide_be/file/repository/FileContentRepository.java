package com.deepdirect.deepwebide_be.file.repository;

import com.deepdirect.deepwebide_be.file.domain.FileContent;
import com.deepdirect.deepwebide_be.file.domain.FileNode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FileContentRepository extends JpaRepository<FileContent, Long> {

    void deleteByFileNode(FileNode fileNode);

    Optional<FileContent> findByFileNode(FileNode fileNode);

}
