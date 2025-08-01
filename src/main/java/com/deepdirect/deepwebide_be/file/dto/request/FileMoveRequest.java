package com.deepdirect.deepwebide_be.file.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "파일 또는 폴더 이동 요청 DTO")
public class FileMoveRequest {

    @Schema(description = "이동할 폴더 ID (필수) ", example = "1")
    @NotNull(message = "이동할 폴더 ID는 필수입니다.") // 추가
    private Long newParentId;
}
