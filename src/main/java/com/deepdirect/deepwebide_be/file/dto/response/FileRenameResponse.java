package com.deepdirect.deepwebide_be.file.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "파일 또는 폴더 이름 변경 응답 DTO")
public class FileRenameResponse {

    @Schema(description = "파일 ID", example = "42")
    private Long fileId;

    @Schema(description = "변경된 파일 이름", example = "main.js")
    private String fileName;

    @Schema(description = "전체 경로", example = "/src/index.js")
    private String path;
}