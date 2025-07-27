package com.deepdirect.deepwebide_be.file.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileTreeNodeResponse {
    private Long fileId;
    private String fileName;
    private String fileType; // "FILE" or "FOLDER"
    private Long parentId; // null for root
    private String path;
    private List<FileTreeNodeResponse> children; // null 또는 빈 배열 가능
}
