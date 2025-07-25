package com.deepdirect.deepwebide_be.repository.controller;

import com.deepdirect.deepwebide_be.global.dto.ApiResponseDto;
import com.deepdirect.deepwebide_be.global.security.CustomUserDetails;
import com.deepdirect.deepwebide_be.global.security.JwtTokenProvider;
import com.deepdirect.deepwebide_be.repository.domain.RepositoryMemberRole;
import com.deepdirect.deepwebide_be.repository.dto.request.RepositoryCreateRequest;
import com.deepdirect.deepwebide_be.repository.dto.request.RepositoryRenameRequest;
import com.deepdirect.deepwebide_be.repository.dto.response.*;
import com.deepdirect.deepwebide_be.repository.service.RepositoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@CrossOrigin(
        origins = {
                "http://localhost:5173",
                "https://www.deepwebide.site"
        },
        allowCredentials = "true"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/repositories")
@Tag(name = "Repository", description = "레포지토리 생성, 조회, 공유, 삭제, 환경설정 등 기능 API")
public class RepositoryController {

    private final RepositoryService repositoryService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping
    @Operation(summary = "레포지토리 생성", description = "사용자가 개인 레포지토리를 생성합니다.")
    public ResponseEntity<ApiResponseDto<RepositoryCreateResponse>> createRepository(
            @RequestBody @Valid RepositoryCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long ownerId = userDetails.getId();

        RepositoryCreateResponse response = repositoryService.createRepository(request, ownerId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.of(201, "레포지토리가 생성되었습니다.", response));
    }

    @GetMapping("/shared")
    @Operation(summary = "공유 레포 조회", description = "공유된 레포지토리 목록을 페이지 단위로 조회합니다.")
    public ResponseEntity<ApiResponseDto<RepositoryListResponse>> getSharedRepositories(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "7") int size,
            @RequestParam(required = false) Boolean liked
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("updatedAt"), Sort.Order.asc("repositoryName")));
        RepositoryListResponse response = repositoryService.getSharedRepositories(userDetails.getId(), pageable, liked);
        return ResponseEntity.ok(ApiResponseDto.of(200, "공유 중인 레포 페이지 조회에 성공했습니다.", response));
    }

    @GetMapping("/shared/me")
    @Operation(summary = "공유받은 레포 조회", description = "공유받은 레포지토리 목록을 페이지 단위로 조회합니다.")
    public ResponseEntity<ApiResponseDto<RepositoryListResponse>> getReceivedSharedRepositories(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "7") int size,
            @RequestParam(required = false) Boolean liked

    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("updatedAt"), Sort.Order.asc("repositoryName")));
        RepositoryListResponse response = repositoryService.getReceivedSharedRepositories(userDetails.getId(), pageable, liked);
        return ResponseEntity.ok(ApiResponseDto.of(200, "공유받은 레포 페이지 조회에 성공했습니다.", response));
    }

    @GetMapping("/mine")
    @Operation(summary = "개인 레포 조회", description = "사용자의 개인 레포지토리 목록을 페이지 단위로 조회합니다.")
    public ResponseEntity<ApiResponseDto<RepositoryListResponse>> getMyRepositories(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "7") int size,
            @RequestParam(required = false) Boolean liked
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("updatedAt"), Sort.Order.asc("repositoryName")));
        RepositoryListResponse response = repositoryService.getMyRepositories(userDetails.getId(), pageable, liked);
        return ResponseEntity.ok(ApiResponseDto.of(200, "개인 레포 페이지 조회에 성공했습니다.", response));
    }

    @PatchMapping("/{repositoryId}")
    @Operation(summary = "레포지토리 이름 변경", description = "오너가 본인의 레포지토리 이름을 변경합니다.")
    public ResponseEntity<ApiResponseDto<RepositoryRenameResponse>> renameRepository(
            @PathVariable Long repositoryId,
            @RequestBody @Valid RepositoryRenameRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        RepositoryRenameResponse response = repositoryService.renameRepository(repositoryId, userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponseDto.of(200, "레포지토리 이름이 변경 되었습니다.", response));
    }

    @PostMapping("/{repositoryId}")
    @Operation(summary = "레포지토리 공유 상태 변경", description = "오너가 레포지토리 공유 상태를 토글합니다. 공유 중이면 공유가 취소되고, 비공유 상태면 공유로 전환됩니다.")
    public ResponseEntity<ApiResponseDto<RepositoryResponse>> toggleRepositoryShare(
            @PathVariable Long repositoryId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        RepositoryResponse response = repositoryService.toggleShareStatus(repositoryId, userDetails.getId());

        String message = response.isIsShared()
                ? "공유 레포지토리로 전환되었습니다."
                : "레포지토리 공유가 취소되었습니다.";

        return ResponseEntity.ok(ApiResponseDto.of(200,message, response));
    }

    @DeleteMapping("/{repositoryId}")
    @Operation(summary = "레포지토리 삭제", description = "오너가 자신의 개인 레포지토리를 삭제합니다.")
    public ResponseEntity<ApiResponseDto<Void>> deleteRepository(
            @PathVariable Long repositoryId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getId();

        repositoryService.deleteRepository(repositoryId, userId);

        return ResponseEntity.ok(ApiResponseDto.of(200,"레포지토리가 삭제되었습니다.",null));
    }

    @PostMapping("/{repositoryId}/exit")
    @Operation(summary = "공유 레포지토리 나가기", description = "사용자가 공유받은 레포지토리에서 퇴장합니다.")
    public ResponseEntity<ApiResponseDto<Void>> exitSharedRepository(
            @PathVariable Long repositoryId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        repositoryService.exitSharedRepository(repositoryId, userDetails.getId());
        return ResponseEntity.ok(ApiResponseDto.of(200, "공유 레포지토리에서 퇴장했습니다.", null));
    }

    @PostMapping("/{repositoryId}/kicked/{memberId}")
    @Operation(summary = "멤버 추방", description = "오너가 특정 멤버를 추방합니다.")
    public ResponseEntity<ApiResponseDto<KickedMemberResponse>> kickMember(
            @PathVariable Long repositoryId,
            @PathVariable Long memberId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        KickedMemberResponse response = repositoryService.kickMember(repositoryId, userDetails.getId(), memberId);
        return ResponseEntity.ok(ApiResponseDto.of(200, "멤버가 성공적으로 추방되었습니다.", response));
    }

    @GetMapping("/{repositoryId}/settings")
    @Operation(summary = "레포지토리 환경설정 정보 조회")
    public ResponseEntity<ApiResponseDto<RepositorySettingResponse>> getRepositorySettings(
            @PathVariable Long repositoryId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        RepositorySettingResponse response = repositoryService.getRepositorySettings(repositoryId, userDetails.getId());

        // 응답 메시지 분기
        String message;
        if (!response.isIsShared()) {
            message = "개인 환경설정 페이지 조회에 성공했습니다.";
        } else {
            boolean isOwner = response.getMembers().stream()
                    .anyMatch(m -> m.getUserId().equals(userDetails.getId()) && m.getRole() == RepositoryMemberRole.OWNER);
            message = isOwner
                    ? "공유한 환경설정 페이지 조회에 성공했습니다."
                    : "공유받은 환경설정 페이지 조회에 성공했습니다.";
        }

        return ResponseEntity.ok(ApiResponseDto.of(200, message, response));
    }
}

