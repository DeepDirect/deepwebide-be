package com.deepdirect.deepwebide_be.repository.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "레포지토리 리스트 조회 응답 DTO")
public class RepositoryListResponse {

    @Schema(description = "현재 페이지 번호", example = "0")
    private int currentPage;

    @Schema(description = "페이지당 레포지토리 개수", example = "7")
    private int pageSize;

    @Schema(description = "전체 페이지 수", example = "5")
    private int totalPages;

    @Schema(description = "전체 레포지토리 수", example = "32")
    private long totalElements;

    @Schema(description = "레포지토리 응답 목록")
    private List<RepositoryResponse> repositories;
}
