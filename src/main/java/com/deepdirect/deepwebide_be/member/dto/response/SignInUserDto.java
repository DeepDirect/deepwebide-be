package com.deepdirect.deepwebide_be.member.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignInUserDto {
    private Long id;
    private String username;
    private String email;
    private String nickname;
    private String profileImageUrl;
}
