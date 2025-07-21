package com.deepdirect.deepwebide_be.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class SignInRequest {

    @NotBlank
    @Email
    @Schema(description = "가입한 이메일", example = "test@test.com")
    private String email;

    @NotBlank
    @Schema(description = "비밀번호", example = "1Q2w3e4r!@#")
    private String password;
}
