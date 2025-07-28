package com.deepdirect.deepwebide_be.history.controller;

import com.deepdirect.deepwebide_be.global.dto.ApiResponseDto;
import com.deepdirect.deepwebide_be.global.security.CustomUserDetails;
import com.deepdirect.deepwebide_be.history.dto.request.HistorySaveRequest;
import com.deepdirect.deepwebide_be.history.dto.response.HistoryDetailResponse;
import com.deepdirect.deepwebide_be.history.dto.response.HistoryListResponse;
import com.deepdirect.deepwebide_be.history.dto.response.HistorySaveResponse;
import com.deepdirect.deepwebide_be.history.service.HistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/repositories")
@RequiredArgsConstructor
@Tag(name = "History", description = "레포지토리 형상관리(히스토리) 저장/조회 API")
public class HistoryController {

    private final HistoryService historyService;

    @Operation(summary = "전체 파일/폴더 저장(히스토리 생성)", description = "현재 파일/폴더 전체 상태를 저장하며 형상관리(히스토리) 기록을 남깁니다.")
    @PostMapping("/{repositoryId}/save")
    public ResponseEntity<ApiResponseDto<HistorySaveResponse>> saveHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long repositoryId,
            @RequestBody HistorySaveRequest request
    ) {
        HistorySaveResponse response = historyService.saveHistory(repositoryId, userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponseDto.of(200, "저장에 성공 했습니다. (히스토리 생성됨)", response));
    }

    @Operation(summary = "히스토리 단건 상세 조회")
    @GetMapping("/{repositoryId}/histories/{historyId}")
    public ResponseEntity<ApiResponseDto<HistoryDetailResponse>> getHistoryDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long repositoryId,
            @PathVariable Long historyId
    ) {
        HistoryDetailResponse resp = historyService.getHistoryDetail(repositoryId, historyId, userDetails.getId());
        return ResponseEntity.ok(ApiResponseDto.of(200, "히스토리 조회 성공", resp));
    }

    @Operation(summary = "히스토리(형상관리) 목록 조회")
    @GetMapping("/{repositoryId}/histories")
    public ResponseEntity<ApiResponseDto<List<HistoryListResponse>>> getHistories(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long repositoryId
    ) {
        List<HistoryListResponse> resp = historyService.getHistories(repositoryId, userDetails.getId());
        return ResponseEntity.ok(ApiResponseDto.of(200, "히스토리 목록 조회 성공", resp));
    }
}
