package com.deepdirect.deepwebide_be.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignUpResponse {

    @Schema(description = "사용자 id", example = "1")
    private Long id;

    @Schema(description = "사용자 실명", example = "홍길동")
    private String username;

    @Schema(description = "닉네임", example = "고통스러운 개발자")
    private String nickname;

    @Schema(description = "프로필 이미지 URL", example = "https://cdn.pixabay.com/photo/2013/07/12/14/15/boy-148071_1280.png")
    private String profileImageUrl;
}
