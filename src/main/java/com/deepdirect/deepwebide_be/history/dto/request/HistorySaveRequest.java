package com.deepdirect.deepwebide_be.history.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@Schema(description = "히스토리 저장 요청 DTO")
public class HistorySaveRequest {

    @Schema(description = "저장 메시지(커밋 메시지)", example = "1차 개발 완료")
    private String message;

    @Schema(description = "저장할 파일/폴더 노드 목록 (트리 구조 아님, 전체 파일/폴더의 납작한 배열)")
    private List<NodeDto> nodes;

    @Getter
    @NoArgsConstructor
    @Schema(description = "파일 또는 폴더 노드 정보")
    public static class NodeDto {

        @Schema(description = "파일/폴더 ID (생성 직후는 null일 수 있음, 기존 파일은 PK)",
                example = "4")
        private Long fileId;

        @Schema(description = "파일/폴더명", example = "Main.java")
        private String fileName;

        @Schema(description = "노드 타입 (FILE: 파일, FOLDER: 폴더)", example = "FILE")
        private String fileType;

        @Schema(description = "부모 폴더 ID (최상위면 null)", example = "1")
        private Long parentId;

        @Schema(description = "파일/폴더 전체 경로", example = "src/Main.java")
        private String path;

        @Schema(description = "파일 내용 (파일일 때만 존재, 폴더면 null)",
                example = "public class Main { ... }",
                nullable = true)
        private String content;
    }
}
