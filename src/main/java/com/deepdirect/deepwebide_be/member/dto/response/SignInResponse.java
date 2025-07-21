package com.deepdirect.deepwebide_be.member.dto.response;

import lombok.Getter;

@Getter
public class SignInResponse {
    private String accessToken;
    private SignInUserDto user;
}
