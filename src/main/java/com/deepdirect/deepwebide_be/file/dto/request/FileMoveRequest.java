package com.deepdirect.deepwebide_be.file.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "파일 또는 폴더 이동 요청 DTO")
public class FileMoveRequest {

    @Schema(description = "이동할 폴더 ID (최상위면 null) ", example = "1")
    private Long newParentId;

}
