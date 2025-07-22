package com.deepdirect.deepwebide_be.repository.controller;

import com.deepdirect.deepwebide_be.global.dto.ApiResponseDto;
import com.deepdirect.deepwebide_be.global.security.CustomUserDetails;
import com.deepdirect.deepwebide_be.repository.dto.response.RepositoryEntryCodeResponse;
import com.deepdirect.deepwebide_be.repository.service.RepositoryEntryCodeService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/repositories")
public class RepositoryEntryCodeController {

    private final RepositoryEntryCodeService entryCodeService;

    @GetMapping("/{repositoryId}/entrycode")
    @Operation(summary = "입장 코드 확인", description = "공유된 레포지토리의 입장 코드를 오너가 확인합니다.")
    public ResponseEntity<ApiResponseDto<RepositoryEntryCodeResponse>> getEntryCode(
            @PathVariable Long repositoryId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        RepositoryEntryCodeResponse response = entryCodeService.getEntryCode(repositoryId, userDetails.getId());
        return ResponseEntity.ok(ApiResponseDto.of(200, "입장코드 확인이 성공했습니다.", response));
    }
}
