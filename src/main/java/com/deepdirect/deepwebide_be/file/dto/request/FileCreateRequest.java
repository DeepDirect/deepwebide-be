package com.deepdirect.deepwebide_be.file.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
@Schema(description = "파일 또는 폴더 생성 요청 DTO")
public class FileCreateRequest {

    @Schema(description = "파일 또는 폴더 이름", example = "main.js")
    @NotBlank(message = "파일 이름은 필수입니다.")
    private String fileName;

    @Schema(description = "파일 타입 (FILE 또는 FOLDER)", example = "FILE")
    @NotBlank(message = "파일 타입은 필수입니다.")
    private String fileType;

    @Schema(description = "부모 폴더 ID (최상위면 null)", example = "1")
    private Long parentId; // null 허용 -> 검증 X
}
