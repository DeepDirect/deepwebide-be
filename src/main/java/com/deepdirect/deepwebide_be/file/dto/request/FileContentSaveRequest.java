package com.deepdirect.deepwebide_be.file.dto.request;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "실시간 파일 저장 요청 DTO")
public class FileContentSaveRequest {

    @Schema(description = "파일 Content", example = "Hello, World!")
    private String content;
}