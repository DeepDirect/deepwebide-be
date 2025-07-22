package com.deepdirect.deepwebide_be.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@Schema
@AllArgsConstructor
public class PhoneVerifyCodeResponse {

    @Schema(description = "인증 확인 여부", example =  "true")
    private boolean verified;
}