package com.deepdirect.deepwebide_be.file.service;

import com.deepdirect.deepwebide_be.file.domain.FileNode;
import com.deepdirect.deepwebide_be.file.dto.response.FileTreeNodeResponse;
import com.deepdirect.deepwebide_be.file.repository.FileNodeRepository;
import com.deepdirect.deepwebide_be.global.exception.ErrorCode;
import com.deepdirect.deepwebide_be.global.exception.GlobalException;
import com.deepdirect.deepwebide_be.repository.domain.Repository;
import com.deepdirect.deepwebide_be.repository.repository.RepositoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class FileService {

    private final RepositoryRepository repositoryRepository;
    private final FileNodeRepository fileNodeRepository;

    public List<FileTreeNodeResponse> getFileTree(Long repositoryId, Long userId) {
        // 1. 레포지토리/권한 체크
        Repository repo = (Repository) repositoryRepository.findByIdAndMemberOrOwner(repositoryId, userId)
                .orElseThrow(() -> new GlobalException(ErrorCode.REPOSITORY_NOT_FOUND));
        // 2. 해당 레포의 모든 FileNode 조회 (1쿼리)
        List<FileNode> allNodes = fileNodeRepository.findAllByRepositoryId(repositoryId);

        // 3. id → FileTreeNodeResponse 변환 및 맵핑
        Map<Long, FileTreeNodeResponse> idToNode = new HashMap<>();
        for (FileNode node : allNodes) {
            idToNode.put(node.getId(),
                    FileTreeNodeResponse.builder()
                            .fileId(node.getId())
                            .fileName(node.getName())
                            .fileType(node.getFileType().name())
                            .parentId(node.getParent() == null ? null : node.getParent().getId())
                            .path(node.getPath())
                            .children(new ArrayList<>())
                            .build()
            );
        }
        // 4. 부모-자식 연결 및 루트 노드 추출
        List<FileTreeNodeResponse> roots = new ArrayList<>();
        for (FileTreeNodeResponse node : idToNode.values()) {
            if (node.getParentId() == null) {
                roots.add(node);
            } else {
                FileTreeNodeResponse parent = idToNode.get(node.getParentId());
                if (parent != null) parent.getChildren().add(node);
            }
        }
        return roots;
    }

}
