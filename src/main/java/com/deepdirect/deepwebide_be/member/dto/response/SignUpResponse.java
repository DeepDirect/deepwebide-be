package com.deepdirect.deepwebide_be.member.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignUpResponse {
    private Long id;
    private String username;
    private String nickname;
}
