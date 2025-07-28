package com.deepdirect.deepwebide_be.file.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "파일 내용 응답 DTO")
public class FileContentResponse {

    @Schema(description = "파일 ID", example = "42")
    private Long fileId;

    @Schema(description = "파일 이름", example = "main.js")
    private String fileName;

    @Schema(description = "파일 경로 (전체 경로)", example = "/src/components/main.js")
    private String path;

    @Schema(description = "파일 내용", example = "console.log('Hello, World!');")
    private String content;
}
