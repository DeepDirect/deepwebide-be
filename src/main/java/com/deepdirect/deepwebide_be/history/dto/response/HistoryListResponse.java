package com.deepdirect.deepwebide_be.history.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "히스토리 목록 응답 DTO")
public class HistoryListResponse {

    @Schema(description = "히스토리 ID", example = "1")
    private Long historyId;

    @Schema(description = "저장 메시지(커밋 메시지)", example = "1차 개발 완료")
    private String message;

    @Schema(description = "저장 일시", example = "2023-10-01T12:00:00Z")
    private String createdAt;

    @Schema(description = "저장한 사용자 정보")
    private CreatedByDto createdBy;

    @Getter
    @Builder
    @Schema(description = "저장한 사용자 정보 DTO")
    public static class CreatedByDto {

        @Schema(description = "사용자 ID", example = "123")
        private Long userId;

        @Schema(description = "사용자 닉네임", example = "엉뚱한 개발자")
        private String nickname;
    }
}
