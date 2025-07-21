package com.deepdirect.deepwebide_be.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@Schema
@AllArgsConstructor
public class SignInResponse {

    @Schema(description = "Access Token (JWT)", example =  "eyJhbGciOi...")
    private String accessToken;

    @Schema(description = "로그인 사용자 정보")
    private SignInUserDto user;
}
