package com.deepdirect.deepwebide_be.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import com.deepdirect.deepwebide_be.member.domain.User;
import lombok.Getter;

@Getter
@Schema
public class SignInUserDto {

    @Schema(description = "사용자 id", example = "1")
    private final Long id;

    @Schema(description = "사용자 실명", example = "홍길동")
    private final String username;

    @Schema(description = "이메일", example = "test@test.com")
    private final String email;

    @Schema(description = "닉네임", example = "고통스러운 개발자")
    private final String nickname;

    @Schema(description = "프로필 이미지 URL", example = "https://cdn.pixabay.com/photo/2013/07/12/14/15/boy-148071_1280.png")
    private final String profileImageUrl;

    public SignInUserDto(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.profileImageUrl = user.getProfileImageUrl();
    }
}
