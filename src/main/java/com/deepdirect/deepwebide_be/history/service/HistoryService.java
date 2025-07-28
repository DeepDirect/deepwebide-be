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
import com.deepdirect.deepwebide_be.history.dto.response.HistoryDetailResponse;
import com.deepdirect.deepwebide_be.history.dto.response.HistoryListResponse;
import com.deepdirect.deepwebide_be.history.dto.response.HistoryRestoreResponse;
import com.deepdirect.deepwebide_be.history.dto.response.HistorySaveResponse;
import com.deepdirect.deepwebide_be.history.repository.HistoryFileRepository;
import com.deepdirect.deepwebide_be.history.repository.HistoryRepository;
import com.deepdirect.deepwebide_be.member.domain.User;
import com.deepdirect.deepwebide_be.member.repository.UserRepository;
import com.deepdirect.deepwebide_be.repository.domain.Repository;
import com.deepdirect.deepwebide_be.repository.repository.RepositoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
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
    private final UserRepository userRepository;

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

        idToNode.put(clientFileId, fileNode);

        if (fileNode.getFileType() == FileType.FILE) {
            FileContent content = FileContent.builder()
                    .fileNode(fileNode)
                    .content(dto.getContent() == null ? new byte[0] : dto.getContent().getBytes(StandardCharsets.UTF_8))
                    .build();
            fileContentRepository.save(content);
        }
    }

    @Transactional(readOnly = true)
    public HistoryDetailResponse getHistoryDetail(Long repositoryId, Long historyId, Long userId) {
        // 1. 권한 체크 & 레포 확인
        Repository repo = repositoryRepository.findByIdAndMemberOrOwner(repositoryId, userId)
                .orElseThrow(() -> new GlobalException(ErrorCode.REPOSITORY_NOT_FOUND));

        // 2. 히스토리 조회
        History history = historyRepository.findById(historyId)
                .orElseThrow(() -> new GlobalException(ErrorCode.HISTORY_NOT_FOUND));

        if (!history.getRepository().getId().equals(repositoryId)) {
            throw new GlobalException(ErrorCode.HISTORY_NOT_FOUND);
        }

        // 3. 파일 목록 조회 (파일만)
        List<HistoryFile> files = historyFileRepository.findByHistory(history);
        List<HistoryDetailResponse.HistoryFileDto> fileDtos = files.stream()
                .filter(f -> "FILE".equals(f.getFileType()))
                .map(f -> HistoryDetailResponse.HistoryFileDto.builder()
                        .path(f.getPath())
                        .content(f.getContent())
                        .build())
                .toList();

        // 4. 응답 DTO 빌드
        return HistoryDetailResponse.builder()
                .historyId(history.getId())
                .message(history.getMessage())
                .createdAt(history.getCreatedAt().toString())
                .files(fileDtos)
                .build();
    }

    @Transactional(readOnly = true)
    public List<HistoryListResponse> getHistories(Long repositoryId, Long userId) {
        // 1. 권한 체크 & 레포 확인
        Repository repo = repositoryRepository.findByIdAndMemberOrOwner(repositoryId, userId)
                .orElseThrow(() -> new GlobalException(ErrorCode.REPOSITORY_NOT_FOUND));

        // 2. 히스토리 목록(최신순) 조회
        List<History> histories = historyRepository.findByRepositoryOrderByCreatedAtDesc(repo);

        // 3. 변환
        return histories.stream()
                .map(history -> {
                    // 작성자 정보 조회 (예시: authorId → User 엔티티 조회, 닉네임 등)
                    User user = userRepository.findById(history.getAuthorId()).orElse(null);
                    return HistoryListResponse.builder()
                            .historyId(history.getId())
                            .message(history.getMessage())
                            .createdAt(history.getCreatedAt().toString())
                            .createdBy(HistoryListResponse.CreatedByDto.builder()
                                    .userId(history.getAuthorId())
                                    .nickname(user != null ? user.getNickname() : "알 수 없음")
                                    .build())
                            .build();
                })
                .toList();
    }

    @Transactional
    public HistoryRestoreResponse restoreHistory(Long repositoryId, Long historyId, Long userId) {
        // 1. 레포/오너 확인
        Repository repo = repositoryRepository.findById(repositoryId)
                .orElseThrow(() -> new GlobalException(ErrorCode.REPOSITORY_NOT_FOUND));

        if (!repo.getOwner().getId().equals(userId)) {
            throw new GlobalException(ErrorCode.FORBIDDEN);
        }

        // 2. 해당 히스토리 및 파일 목록 조회
        History history = historyRepository.findById(historyId)
                .orElseThrow(() -> new GlobalException(ErrorCode.HISTORY_NOT_FOUND));

        if (!history.getRepository().getId().equals(repositoryId)) {
            throw new GlobalException(ErrorCode.HISTORY_NOT_FOUND);
        }

        List<HistoryFile> historyFiles = historyFileRepository.findByHistory(history);

        // 3. 기존 파일/폴더 모두 삭제 (안전하게 하위부터)
        List<FileNode> dbNodes = fileNodeRepository.findAllByRepositoryId(repositoryId);
        dbNodes.stream()
                .sorted((a, b) -> b.getPath().length() - a.getPath().length())
                .forEach(node -> {
                    if (node.getFileType() == FileType.FILE) fileContentRepository.deleteByFileNode(node);
                    fileNodeRepository.delete(node);
                });

        // 4. 히스토리 파일/폴더를 트리로 다시 생성 (parent-child 순서 보장)
        Map<Long, FileNode> tempIdToNode = new HashMap<>();
        // id → dto 맵
        Map<Long, HistoryFile> idToDto = historyFiles.stream()
                .collect(Collectors.toMap(HistoryFile::getFileId, f -> f));
        // parent-child 순서로 재귀 복원
        for (Long fileId : idToDto.keySet()) {
            restoreNodeRecursive(fileId, idToDto, tempIdToNode, repo);
        }

        return HistoryRestoreResponse.builder()
                .historyId(historyId)
                .restoredAt(LocalDateTime.now().toString())
                .build();
    }

    // 재귀 복원 함수
    private void restoreNodeRecursive(Long fileId,
                                      Map<Long, HistoryFile> idToDto,
                                      Map<Long, FileNode> idToNode,
                                      Repository repo) {
        if (idToNode.containsKey(fileId)) return;
        HistoryFile dto = idToDto.get(fileId);
        FileNode parent = null;
        if (dto.getParentId() != null) {
            restoreNodeRecursive(dto.getParentId(), idToDto, idToNode, repo);
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
        idToNode.put(fileId, fileNode);

        if ("FILE".equals(dto.getFileType())) {
            FileContent content = FileContent.builder()
                    .fileNode(fileNode)
                    .content(dto.getContent() == null ? new byte[0] : dto.getContent().getBytes(StandardCharsets.UTF_8))
                    .build();
            fileContentRepository.save(content);
        }
    }

}
