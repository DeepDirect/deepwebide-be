package com.deepdirect.deepwebide_be.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EmailCheckResponse {

    @Schema(description = "가입 가능 여부", example = "true")
    private boolean isAvailable;
}
