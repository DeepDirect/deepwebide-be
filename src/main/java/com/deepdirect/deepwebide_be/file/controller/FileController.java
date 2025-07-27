package com.deepdirect.deepwebide_be.file.controller;

import com.deepdirect.deepwebide_be.file.dto.request.FileCreateRequest;
import com.deepdirect.deepwebide_be.file.dto.request.FileMoveRequest;
import com.deepdirect.deepwebide_be.file.dto.request.FileRenameRequest;
import com.deepdirect.deepwebide_be.file.dto.response.FileContentResponse;
import com.deepdirect.deepwebide_be.file.dto.response.FileNodeResponse;
import com.deepdirect.deepwebide_be.file.dto.response.FileRenameResponse;
import com.deepdirect.deepwebide_be.file.dto.response.FileTreeNodeResponse;
import com.deepdirect.deepwebide_be.file.service.FileService;
import com.deepdirect.deepwebide_be.global.dto.ApiResponseDto;
import com.deepdirect.deepwebide_be.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/repositories")
@Tag(name = "File", description = "파일/폴더 생성, 조회, 이름 변경, 삭제, 이동 등 기능 API")
public class FileController {

    private final FileService fileService;

    @Operation(summary = "파일 트리 조회", description = "각 레포지토리의 파일 트리를 조회합니다.")
    @GetMapping("/{repositoryId}/files")
    public ResponseEntity<ApiResponseDto<List<FileTreeNodeResponse>>> getFileTree(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long repositoryId
    ) {
        List<FileTreeNodeResponse> fileTree = fileService.getFileTree(repositoryId, userDetails.getId());
        return ResponseEntity.ok(ApiResponseDto.of(200, "파일 트리 조회 성공했습니다.", fileTree));
    }

    @Operation(summary = "파일/폴더 생성", description = "파일 또는 폴더를 생성합니다.")
    @PostMapping("/{repositoryId}/files")
    public ResponseEntity<ApiResponseDto<FileNodeResponse>> createFileOrFolder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long repositoryId,
            @RequestBody FileCreateRequest request
    ) {
        FileNodeResponse response = fileService.createFileOrFolder(repositoryId, userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponseDto.of(201, "파일 및 폴더 생성이 성공했습니다.", response));
    }

    @Operation(summary = "파일/폴더 이름 변경", description = "파일 또는 폴더의 이름을 변경합니다.")
    @PatchMapping("/{repositoryId}/files/{fileId}/rename")
    public ResponseEntity<ApiResponseDto<FileRenameResponse>> renameFileOrFolder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long repositoryId,
            @PathVariable Long fileId,
            @RequestBody FileRenameRequest request
    ) {
        FileRenameResponse response = fileService.renameFileOrFolder(
                repositoryId,
                fileId,
                userDetails.getId(),
                request.getNewFileName()
        );
        return ResponseEntity.ok(ApiResponseDto.of(200, "이름이 변경되었습니다.", response));
    }

    @Operation(summary = "파일/폴더 삭제", description = "특정 파일/폴더 및 하위 모두 삭제")
    @DeleteMapping("/{repositoryId}/files/{fileId}")
    public ResponseEntity<ApiResponseDto<Void>> deleteFileOrFolder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long repositoryId,
            @PathVariable Long fileId
    ) {
        fileService.deleteFileOrFolder(repositoryId, fileId, userDetails.getId());
        return ResponseEntity.ok(ApiResponseDto.of(200, "삭제가 완료되었습니다.", null));
    }

    @Operation(summary = "파일/폴더 이동", description = "파일 또는 폴더를 다른 위치로 이동합니다.")
    @PatchMapping("/{repositoryId}/files/{fileId}/move")
    public ResponseEntity<ApiResponseDto<FileNodeResponse>> moveFileOrFolder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long repositoryId,
            @PathVariable Long fileId,
            @RequestBody FileMoveRequest req
    ) {
        FileNodeResponse result = fileService.moveFileOrFolder(
                repositoryId, fileId, userDetails.getId(), req.getNewParentId());
        return ResponseEntity.ok(ApiResponseDto.of(200, "이동이 완료되었습니다.", result));
    }

    @Operation(summary = "파일 내용 조회", description = "파일의 내용을 조회합니다.")
    @GetMapping("/{repositoryId}/files/{fileId}/content")
    public ResponseEntity<ApiResponseDto<FileContentResponse>> getFileContent(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long repositoryId,
            @PathVariable Long fileId
    ) {
        FileContentResponse response = fileService.getFileContent(repositoryId, fileId, userDetails.getId());
        return ResponseEntity.ok(ApiResponseDto.of(200, "파일 내용 조회 성공", response));
    }
}
