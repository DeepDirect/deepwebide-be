package com.deepdirect.deepwebide_be.history.controller;

import com.deepdirect.deepwebide_be.global.dto.ApiResponseDto;
import com.deepdirect.deepwebide_be.global.security.CustomUserDetails;
import com.deepdirect.deepwebide_be.history.dto.request.HistorySaveRequest;
import com.deepdirect.deepwebide_be.history.dto.response.HistorySaveResponse;
import com.deepdirect.deepwebide_be.history.service.HistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
}
