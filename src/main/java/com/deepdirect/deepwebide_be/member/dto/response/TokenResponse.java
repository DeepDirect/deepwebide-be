package com.deepdirect.deepwebide_be.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenResponse {
    @Schema(description = "재발급된 토큰", example = "ACCESS_TOKEN_VALUE")
    private String accessToken;
}
