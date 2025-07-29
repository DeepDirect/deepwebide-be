package com.deepdirect.deepwebide_be.history.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "히스토리 상세 조회 응답 DTO")
public class HistoryDetailResponse {

    @Schema(description = "히스토리 ID", example = "1")
    private Long historyId;

    @Schema(description = "저장 메시지(커밋 메시지)", example = "1차 개발 완료")
    private String message;

    @Schema(description = "저장 일시", example = "2023-10-01T12:00:00Z")
    private String createdAt;

    @Schema(description = "저장된 파일/폴더 경로 및 내용 목록")
    private List<HistoryFileDto> files;

    @Getter
    @Builder
    @Schema(description = "저장된 파일/폴더 경로 및 내용 DTO")
    public static class HistoryFileDto {

        @Schema(description = "파일 경로 (전체 경로)", example = "/src/components/main.js")
        private String path;

        @Schema(description = "파일 내용", example = "console.log('Hello, World!');")
        private String content;
    }
}
