package com.deepdirect.deepwebide_be.file.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
@Schema(description = "파일 또는 폴더 이름 변경 요청 DTO")
public class FileRenameRequest {

    @Schema(description = "파일 또는 폴더 변경될 이름", example = "main.js")
    @NotBlank(message = "파일 이름은 필수입니다.")
    private String newFileName;
}
