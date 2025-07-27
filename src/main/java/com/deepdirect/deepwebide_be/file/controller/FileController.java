package com.deepdirect.deepwebide_be.file.controller;

import com.deepdirect.deepwebide_be.file.dto.response.FileTreeNodeResponse;
import com.deepdirect.deepwebide_be.file.service.FileService;
import com.deepdirect.deepwebide_be.global.dto.ApiResponseDto;
import com.deepdirect.deepwebide_be.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
