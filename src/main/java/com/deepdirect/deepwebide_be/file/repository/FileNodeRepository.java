package com.deepdirect.deepwebide_be.file.repository;

import com.deepdirect.deepwebide_be.file.domain.FileNode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileNodeRepository extends JpaRepository<FileNode, Long> {

    // 특정 레포의 모든 파일/폴더 조회
    List<FileNode> findAllByRepositoryId(Long repositoryId);

    boolean existsByRepositoryIdAndParentIdAndName(Long repositoryId, Long parentId, String name);

    List<FileNode> findAllByParent(FileNode parent);

    boolean existsByRepositoryIdAndParentAndName(Long repositoryId, FileNode parent, String name);

}
