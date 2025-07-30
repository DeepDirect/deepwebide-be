package com.deepdirect.deepwebide_be.file.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "실시간 파일 저장 응답 DTO")
public class FileContentSaveResponse {

    @Schema(description = "파일 ID", example = "1")
    private Long fileId;

    @Schema(description = "파일 이름", example = "example.txt")
    private String fileName;

    @Schema(description = "파일 경로", example = "/path/to/example.txt")
    private String path;

    @Schema(description = "저장 완료 시각 (ISO8601 형식)", example = "2023-10-01T12:00:00Z")
    private String updatedAt;
}
