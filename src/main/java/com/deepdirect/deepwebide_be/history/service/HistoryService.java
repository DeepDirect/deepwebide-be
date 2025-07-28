package com.deepdirect.deepwebide_be.history.service;

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

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final RepositoryRepository repositoryRepository;
    private final HistoryRepository historyRepository;
    private final HistoryFileRepository historyFileRepository;

    @Transactional
    public HistorySaveResponse saveHistory(Long repositoryId, Long userId, HistorySaveRequest request) {
        // 1. 레포 권한 체크
        Repository repo = repositoryRepository.findByIdAndMemberOrOwner(repositoryId, userId)
                .orElseThrow(() -> new GlobalException(ErrorCode.REPOSITORY_NOT_FOUND));

        // 2. History 엔티티 생성
        History history = History.builder()
                .repository(repo)
                .message(request.getMessage())
                .authorId(userId)
                .createdAt(LocalDateTime.now())
                .build();
        history = historyRepository.save(history);

        // 3. HistoryFile 리스트 생성 및 저장
        List<HistorySaveRequest.NodeDto> nodes = request.getNodes();
        for (HistorySaveRequest.NodeDto dto : nodes) {
            HistoryFile historyFile = HistoryFile.builder()
                    .history(history)
                    .fileId(dto.getFileId())
                    .fileName(dto.getFileName())
                    .fileType(dto.getFileType())
                    .parentId(dto.getParentId())
                    .path(dto.getPath())
                    .content(dto.getContent())
                    .build();
            historyFileRepository.save(historyFile);
        }

        return HistorySaveResponse.builder()
                .historyId(history.getId())
                .build();
    }
}
