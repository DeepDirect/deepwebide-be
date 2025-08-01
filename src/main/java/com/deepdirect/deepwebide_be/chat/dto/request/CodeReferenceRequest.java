package com.deepdirect.deepwebide_be.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "코드 참조 요청 정보")
public class CodeReferenceRequest {

    private Long referenceId;
    private String path;
}
