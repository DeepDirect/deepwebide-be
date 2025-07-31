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

}
