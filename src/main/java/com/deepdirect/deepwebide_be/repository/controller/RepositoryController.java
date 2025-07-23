package com.deepdirect.deepwebide_be.repository.controller;

import com.deepdirect.deepwebide_be.global.dto.ApiResponseDto;
import com.deepdirect.deepwebide_be.global.security.CustomUserDetails;
import com.deepdirect.deepwebide_be.global.security.JwtTokenProvider;
import com.deepdirect.deepwebide_be.repository.dto.request.RepositoryCreateRequest;
import com.deepdirect.deepwebide_be.repository.dto.request.RepositoryRenameRequest;
import com.deepdirect.deepwebide_be.repository.dto.response.RepositoryCreateResponse;
import com.deepdirect.deepwebide_be.repository.dto.response.RepositoryListResponse;
import com.deepdirect.deepwebide_be.repository.dto.response.RepositoryRenameResponse;
import com.deepdirect.deepwebide_be.repository.service.RepositoryService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/repositories")
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
            @RequestParam(defaultValue = "7") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("updatedAt"), Sort.Order.asc("repositoryName")));
        RepositoryListResponse response = repositoryService.getSharedRepositories(userDetails.getId(), pageable);
        return ResponseEntity.ok(ApiResponseDto.of(200, "공유 중인 레포 페이지 조회에 성공했습니다.", response));
    }
    @GetMapping("/shared/me")
    @Operation(summary = "공유받은 레포 조회", description = "공유받은 레포지토리 목록을 페이지 단위로 조회합니다.")
    public ResponseEntity<ApiResponseDto<RepositoryListResponse>> getReceivedSharedRepositories(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "7") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("updatedAt"), Sort.Order.asc("repositoryName")));
        RepositoryListResponse response = repositoryService.getReceivedSharedRepositories(userDetails.getId(), pageable);
        return ResponseEntity.ok(ApiResponseDto.of(200, "공유받은 레포 페이지 조회에 성공했습니다.", response));
    }

    @GetMapping("/mine")
    @Operation(summary = "개인 레포 조회", description = "사용자의 개인 레포지토리 목록을 페이지 단위로 조회합니다.")
    public ResponseEntity<ApiResponseDto<RepositoryListResponse>> getMyRepositories(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "7") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("updatedAt"), Sort.Order.asc("repositoryName")));
        RepositoryListResponse response = repositoryService.getMyRepositories(userDetails.getId(), pageable);
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
}
