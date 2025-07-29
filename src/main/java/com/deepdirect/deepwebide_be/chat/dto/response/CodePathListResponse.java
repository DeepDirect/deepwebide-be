package com.deepdirect.deepwebide_be.chat.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CodePathListResponse {
    private List<String> paths;
}