package com.deepdirect.deepwebide_be.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@Schema
@AllArgsConstructor
public class FindEmailResponse {

    @Schema(description = "이메일", example = "test@test.com")
    private final String email;
}
