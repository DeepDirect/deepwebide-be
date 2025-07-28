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

        // 5. id → FileNode 캐싱 (성능/트리 부모-자식 연결 위해)
        Map<Long, FileNode> idToNode = dbNodes.stream()
                .collect(Collectors.toMap(FileNode::getId, n -> n));

        // 6. 요청 기반 파일/폴더 추가 or 수정(이름/경로/내용)
        for (HistorySaveRequest.NodeDto dto : request.getNodes()) {
            FileNode fileNode = idToNode.get(dto.getFileId());
            FileNode parent = (dto.getParentId() == null) ? null : idToNode.get(dto.getParentId());

            if (fileNode == null) {
                // **신규 파일/폴더**
                fileNode = FileNode.builder()
                        .repository(repo)
                        .name(dto.getFileName())
                        .fileType(FileType.valueOf(dto.getFileType()))
                        .parent(parent)
                        .path(dto.getPath())
                        .build();
                fileNode = fileNodeRepository.save(fileNode);
                idToNode.put(fileNode.getId(), fileNode); // ★ map에 추가

                if (fileNode.getFileType() == FileType.FILE) {
                    FileContent content = FileContent.builder()
                            .fileNode(fileNode)
                            .content(dto.getContent() == null ? new byte[0] : dto.getContent().getBytes(StandardCharsets.UTF_8))
                            .build();
                    fileContentRepository.save(content);
                }
            } else {
                // **기존 파일/폴더 - 이름/경로/부모 변경**
                boolean updated = false;
                if (!fileNode.getName().equals(dto.getFileName())) {
                    fileNode.rename(dto.getFileName());
                    updated = true;
                }
                if (parent != fileNode.getParent()) {
                    fileNode.moveToParent(parent, parent == null ? "" : parent.getPath());
                    updated = true;
                }
                if (!fileNode.getPath().equals(dto.getPath())) {
                    fileNode.updatePath(dto.getPath());
                    updated = true;
                }
                // **파일 내용 변경**
                if (fileNode.getFileType() == FileType.FILE && dto.getContent() != null) {
                    FileContent fileContent = fileContentRepository.findByFileNode(fileNode)
                            .orElseThrow(() -> new GlobalException(ErrorCode.FILE_CONTENT_NOT_FOUND));
                    String currentContent = new String(fileContent.getContent(), StandardCharsets.UTF_8);
                    if (!currentContent.equals(dto.getContent())) {
                        fileContent.updateContent(dto.getContent().getBytes(StandardCharsets.UTF_8));
                    }
                }
                // (필요시 fileNodeRepository.save(fileNode);는 생략 가능, JPA dirty checking)
            }
        }

        // 7. History & HistoryFile 기록 (스냅샷처럼)
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
}
