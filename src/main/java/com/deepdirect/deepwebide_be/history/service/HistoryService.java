package com.deepdirect.deepwebide_be.history.service;

import com.deepdirect.deepwebide_be.file.domain.FileContent;
import com.deepdirect.deepwebide_be.file.domain.FileNode;
import com.deepdirect.deepwebide_be.file.domain.FileType;
import com.deepdirect.deepwebide_be.file.repository.FileContentRepository;
import com.deepdirect.deepwebide_be.file.repository.FileNodeRepository;
import com.deepdirect.deepwebide_be.global.exception.ErrorCode;
import com.deepdirect.deepwebide_be.global.exception.GlobalException;
import com.deepdirect.deepwebide_be.history.domain.History;
import com.deepdirect.deepwebide_be.history.domain.HistoryFile;
import com.deepdirect.deepwebide_be.history.dto.request.HistorySaveRequest;
import com.deepdirect.deepwebide_be.history.dto.response.HistorySaveResponse;
import com.deepdirect.deepwebide_be.history.repository.HistoryFileRepository;
import com.deepdirect.deepwebide_be.history.repository.HistoryRepository;
import com.deepdirect.deepwebide_be.repository.domain.Repository;
import com.deepdirect.deepwebide_be.repository.repository.RepositoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final RepositoryRepository repositoryRepository;
    private final HistoryRepository historyRepository;
    private final HistoryFileRepository historyFileRepository;
    private final FileNodeRepository fileNodeRepository;
    private final FileContentRepository fileContentRepository;

    @Transactional
    public HistorySaveResponse saveHistory(Long repositoryId, Long userId, HistorySaveRequest request) {

        for (HistorySaveRequest.NodeDto dto : request.getNodes()) {
            System.out.println(
                    "fileId=" + dto.getFileId() +
                            ", fileName=" + dto.getFileName() +
                            ", parentId=" + dto.getParentId() +
                            ", path=" + dto.getPath()
            );
        }

        // 1. 권한 체크 및 레포 조회
        Repository repo = repositoryRepository.findByIdAndMemberOrOwner(repositoryId, userId)
                .orElseThrow(() -> new GlobalException(ErrorCode.REPOSITORY_NOT_FOUND));

        // 2. 요청에서 전체 fileId set 추출
        Set<Long> requestFileIds = request.getNodes().stream()
                .map(HistorySaveRequest.NodeDto::getFileId)
                .collect(Collectors.toSet());

        // 3. 현재 DB에 있는 FileNode 전체 조회 (삭제 대상 판별용)
        List<FileNode> dbNodes = fileNodeRepository.findAllByRepositoryId(repositoryId);

        // 4. DB에 있지만 요청에 없는 FileNode 삭제 (자식 먼저 삭제)
        List<FileNode> toDelete = dbNodes.stream()
                .filter(node -> !requestFileIds.contains(node.getId()))
                .sorted((a, b) -> b.getPath().length() - a.getPath().length())
                .toList();
        for (FileNode node : toDelete) {
            if (node.getFileType() == FileType.FILE) {
                fileContentRepository.deleteByFileNode(node);
            }
            fileNodeRepository.delete(node);
        }

        // 5. id → FileNode (DB에 이미 있던 것만)
        Map<Long, FileNode> idToNode = dbNodes.stream()
                .collect(Collectors.toMap(FileNode::getId, n -> n));

        // 6. id → 요청 NodeDto
        Map<Long, HistorySaveRequest.NodeDto> idToDto = request.getNodes().stream()
                .collect(Collectors.toMap(HistorySaveRequest.NodeDto::getFileId, n -> n));

        // 7. parent → 자식 순서로 재귀 저장
        for (Long nodeId : idToDto.keySet()) {
            saveNodeRecursive(nodeId, idToNode, idToDto, repo);
        }

        // 8. History & HistoryFile 기록 (스냅샷처럼)
        History history = History.builder()
                .repository(repo)
                .message(request.getMessage())
                .authorId(userId)
                .createdAt(LocalDateTime.now())
                .build();
        history = historyRepository.save(history);

        History finalHistory = history;
        List<HistoryFile> historyFiles = request.getNodes().stream()
                .map(dto -> HistoryFile.builder()
                        .history(finalHistory)
                        .fileId(dto.getFileId())
                        .fileName(dto.getFileName())
                        .fileType(dto.getFileType())
                        .parentId(dto.getParentId())
                        .path(dto.getPath())
                        .content(dto.getContent())
                        .build()
                ).toList();
        historyFileRepository.saveAll(historyFiles);

        return HistorySaveResponse.builder()
                .historyId(history.getId())
                .build();
    }

    private void saveNodeRecursive(
            Long clientFileId,
            Map<Long, FileNode> idToNode,
            Map<Long, HistorySaveRequest.NodeDto> idToDto,
            Repository repo
    ) {
        // 이미 저장된 노드는 무시
        if (idToNode.containsKey(clientFileId)) return;

        HistorySaveRequest.NodeDto dto = idToDto.get(clientFileId);

        FileNode parent = null;
        if (dto.getParentId() != null) {
            saveNodeRecursive(dto.getParentId(), idToNode, idToDto, repo);
            parent = idToNode.get(dto.getParentId());
        }

        FileNode fileNode = FileNode.builder()
                .repository(repo)
                .name(dto.getFileName())
                .fileType(FileType.valueOf(dto.getFileType()))
                .parent(parent)
                .path(dto.getPath())
                .build();
        fileNode = fileNodeRepository.save(fileNode);

        // 🔥 여기서 클라이언트 fileId로 map에 추가!
        idToNode.put(clientFileId, fileNode);

        if (fileNode.getFileType() == FileType.FILE) {
            FileContent content = FileContent.builder()
                    .fileNode(fileNode)
                    .content(dto.getContent() == null ? new byte[0] : dto.getContent().getBytes(StandardCharsets.UTF_8))
                    .build();
            fileContentRepository.save(content);
        }
    }
}
