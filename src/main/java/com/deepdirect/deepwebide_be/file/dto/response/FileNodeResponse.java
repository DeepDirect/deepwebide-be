package com.deepdirect.deepwebide_be.file.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "단일 파일 또는 폴더 정보 응답 DTO")
public class FileNodeResponse {

    @Schema(description = "파일 ID", example = "42")
    private Long fileId;

    @Schema(description = "파일 이름", example = "main.js")
    private String fileName;

    @Schema(description = "파일 타입 (FILE 또는 FOLDER)", example = "FILE")
    private String fileType;

    @Schema(description = "부모 폴더 ID (최상위면 null)", example = "1")
    private Long parentId;

    @Schema(description = "파일 경로 (전체 경로)", example = "/src/components/main.js")
    private String path;
}