package com.deepdirect.deepwebide_be.repository.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RepositoryListResponse {

    private int currentPage;
    private int pageSize;
    private int totalPages;
    private long totalElements;
    private List<RepositoryResponse> repositories;
}
