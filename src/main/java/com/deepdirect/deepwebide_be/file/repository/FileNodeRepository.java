package com.deepdirect.deepwebide_be.file.repository;

import com.deepdirect.deepwebide_be.file.domain.FileNode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface FileNodeRepository extends JpaRepository<FileNode, Long> {

    // 경로+레포로 파일/폴더 노드 찾기 (중복생성 방지용)
    Optional<FileNode> findByRepositoryIdAndPath(Long repositoryId, String path);

    // 특정 레포의 모든 파일/폴더 조회
    List<FileNode> findAllByRepositoryId(Long repositoryId);

    // parent-children 구조 탐색 등 필요시 추가
}
