package com.deepdirect.deepwebide_be.member.dto.response;

import lombok.Getter;

@Getter
public class SignInUserDto {
    private Long id;
    private String username;
    private String email;
    private String nickname;
    private String profileImageUrl;
}
