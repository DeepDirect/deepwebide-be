package com.deepdirect.deepwebide_be.repository.controller;

import com.deepdirect.deepwebide_be.global.dto.ApiResponseDto;
import com.deepdirect.deepwebide_be.global.security.CustomUserDetails;
import com.deepdirect.deepwebide_be.repository.dto.request.EntryCodeVerifyRequest;
import com.deepdirect.deepwebide_be.repository.dto.response.RepositoryAccessCheckResponse;
import com.deepdirect.deepwebide_be.repository.dto.response.RepositoryEntryCodeResponse;
import com.deepdirect.deepwebide_be.repository.dto.response.RepositoryJoinResponse;
import com.deepdirect.deepwebide_be.repository.service.RepositoryEntryCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/repositories")
@Tag(name = "EntryCode", description = "레포지토리 입장 코드 관련 API")
public class RepositoryEntryCodeController {

    private final RepositoryEntryCodeService entryCodeService;

    @GetMapping("/{repositoryId}")
    @Operation(summary = "레포지토리 입장 권한 확인",
            description = "공유 링크로 접근한 사용자가 해당 레포지토리에 입장 가능한지 확인합니다.\n 소유자/멤버 여부에 따라 접근 권한을 판단하며,\n true인 경우 레포지토리 바로 입장, false인 경우 입장 코드 입력 페이지로 이동합니다."
    )
    public ResponseEntity<ApiResponseDto<RepositoryAccessCheckResponse>> checkAccessToRepository(
            @PathVariable Long repositoryId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        RepositoryAccessCheckResponse response = entryCodeService.checkAccess(repositoryId, userDetails.getId());
        String message = response.isAccess() ? "입장 권한이 있습니다." : "입장 권한이 없습니다.";
        return ResponseEntity.ok(ApiResponseDto.of(200, message, response));
    }


    @GetMapping("/{repositoryId}/entrycode")
    @Operation(summary = "입장 코드 확인", description = "레포지토리의 소유자가 공유 상태일 때 입장 코드를 확인합니다. 만료 되었을 경우 재발급 됩니다.")
    public ResponseEntity<ApiResponseDto<RepositoryEntryCodeResponse>> getEntryCode(
            @PathVariable Long repositoryId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        RepositoryEntryCodeResponse response = entryCodeService.getEntryCode(repositoryId, userDetails.getId());
        return ResponseEntity.ok(ApiResponseDto.of(200, "입장코드 확인이 성공했습니다.", response));
    }

    @PostMapping("/{repositoryId}/new-entrycode")
    @Operation(summary = "입장 코드 재발급", description = "공유된 레포지토리의 소유자가 기존 입장 코드를 폐기하고 새로 발급받습니다.")
    public ResponseEntity<ApiResponseDto<Map<String, String>>> regenerateEntryCode(
            @PathVariable Long repositoryId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String newCode = entryCodeService.regenerateEntryCode(repositoryId, userDetails.getId());

        Map<String, String> data = new HashMap<>();
        data.put("newEntryCode", newCode);

        return ResponseEntity.ok(
                ApiResponseDto.of(200, "입장코드가 재발급 되셨습니다.", data)
        );
    }

    @PostMapping("/{repositoryId}/entrycode")
    @Operation(summary = "입장 코드 검증 및 레포 참여", description = "공유된 레포지토리에 입장 코드를 통해 사용자를 참여자로 등록합니다.")
    public ResponseEntity<ApiResponseDto<RepositoryJoinResponse>> verifyEntryCodeAndJoin(
            @PathVariable Long repositoryId,
            @RequestBody @Valid EntryCodeVerifyRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        RepositoryJoinResponse response = entryCodeService.verifyEntryCodeAndJoin(
                repositoryId,
                request.getEntryCode(),
                userDetails.getId()
        );

        return ResponseEntity.ok(
                ApiResponseDto.of(200, "공유 레포지토리에 참여되었습니다.", response)
        );
    }

}
