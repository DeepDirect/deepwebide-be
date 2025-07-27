package com.deepdirect.deepwebide_be.file.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "파일/폴더 트리 노드 응답 DTO")
public class FileTreeNodeResponse {

    @Schema(description = "파일 ID", example = "10")
    private Long fileId;

    @Schema(description = "파일 또는 폴더 이름", example = "src")
    private String fileName;

    @Schema(description = "파일 타입", example = "FOLDER")
    private String fileType;

    @Schema(description = "부모 폴더 ID", example = "1", nullable = true)
    private Long parentId;

    @Schema(description = "전체 경로", example = "/src/index.js")
    private String path;

    @Schema(description = "하위 파일/폴더 목록")
    private List<FileTreeNodeResponse> children;
}
