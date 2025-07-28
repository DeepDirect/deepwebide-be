package com.deepdirect.deepwebide_be.history.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "히스토리 복원 응답 DTO")
public class HistoryRestoreResponse {

    @Schema(description = "복원된 히스토리 ID", example = "12345")
    private Long historyId;

    @Schema(description = "저장된 날짜 및 시간", example = "2023-10-01T12:00:00Z")
    private String restoredAt;
}
