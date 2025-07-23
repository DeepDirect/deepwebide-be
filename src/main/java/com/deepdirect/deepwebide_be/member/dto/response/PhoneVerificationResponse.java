package com.deepdirect.deepwebide_be.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@Schema
@AllArgsConstructor
public class PhoneVerificationResponse {

    @Schema(description = "인증 유효 시간", example =  "180")
    private int expiresIn;
}
