package com.deepdirect.deepwebide_be.file.dto.request;

import lombok.Getter;

@Getter
public class FileCreateRequest {
    private String fileName;
    private String fileType; // "FILE" or "FOLDER"
    private Long parentId;   // null이면 최상위
}
