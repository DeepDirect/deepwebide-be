package com.deepdirect.deepwebide_be.repository.controller;

import com.deepdirect.deepwebide_be.global.dto.ApiResponseDto;
import com.deepdirect.deepwebide_be.global.security.CustomUserDetails;
import com.deepdirect.deepwebide_be.repository.dto.response.RepositoryExecuteResponse;
import com.deepdirect.deepwebide_be.repository.service.RepositoryRunService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/repositories")
@Tag(name = "RUN", description = "레포지토리 실행, 로그 반환 등 기능 API")
public class RepositoryRunController {

    private final RepositoryRunService repositoryRunService;

    @PostMapping("/{repositoryId}/execute")
    public ResponseEntity<ApiResponseDto<RepositoryExecuteResponse>> executeRepository(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long repositoryId
    ) {
        RepositoryExecuteResponse resp = repositoryRunService.executeRepository(repositoryId, userDetails.getId());
        return ResponseEntity.ok(ApiResponseDto.of(200, "레포지토리 실행 요청 완료", resp));
    }

}

