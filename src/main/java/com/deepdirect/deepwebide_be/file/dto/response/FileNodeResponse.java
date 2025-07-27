package com.deepdirect.deepwebide_be.file.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileNodeResponse {
    private Long fileId;
    private String fileName;
    private String fileType;
    private Long parentId;
    private String path;
}