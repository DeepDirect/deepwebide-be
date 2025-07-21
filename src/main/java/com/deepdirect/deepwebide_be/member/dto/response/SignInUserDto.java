package com.deepdirect.deepwebide_be.member.dto.response;

import com.deepdirect.deepwebide_be.member.domain.User;
import lombok.Getter;

@Getter
public class SignInUserDto {
    private Long id;
    private String username;
    private String email;
    private String nickname;
    private String profileImageUrl;

    public SignInUserDto(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.profileImageUrl = user.getProfileImageUrl();
    }
}
