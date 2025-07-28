package com.deepdirect.deepwebide_be.history.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "히스토리 저장 응답 DTO")
public class HistorySaveResponse {

    @Schema(description = "저장된 히스토리 ID", example = "12345")
    private Long historyId;
}
