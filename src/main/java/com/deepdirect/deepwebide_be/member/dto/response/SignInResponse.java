package com.deepdirect.deepwebide_be.member.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignInResponse {
    private String accessToken;
    private SignInUserDto user;
}
