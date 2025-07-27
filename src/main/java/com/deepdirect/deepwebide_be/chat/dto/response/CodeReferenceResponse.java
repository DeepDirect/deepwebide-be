package com.deepdirect.deepwebide_be.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "메시지 참조 코드 정보")
public class CodeReferenceResponse {

    @Schema(description = "참조 ID")
    private final Long referenceId;

    @Schema(description = "파일 경로")
    private final String filePath;

    @Schema(description = "라인 번호", nullable = true)
    private final Integer line;
}
