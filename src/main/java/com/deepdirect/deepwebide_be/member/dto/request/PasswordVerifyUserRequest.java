package com.deepdirect.deepwebide_be.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "비밀번호 찾기 본인 인증 및 reauthToken 발급")
public class PasswordVerifyUserRequest {

    @NotBlank(message = "이름 입력이 누락되었습니다.")
    @Schema(description = "사용자 이름", example = "홍길동")
    private String username;

    @NotBlank(message = "이메일 입력이 누락되었습니다.")
    @Email
    @Schema(description = "가입한 이메일", example = "test@test.com")
    private String email;

    @NotBlank(message = "전화번호 입력이 누락되었습니다.")
    @Pattern(regexp = "^01[0-9]\\d{7,8}$", message = "올바른 형식의 전화번호여야 합니다.")
    private String phoneNumber;

    @NotBlank(message = "인증번호 입력이 누락되었습니다.")
    @Pattern(regexp = "^\\d{6}$", message = "인증번호는 숫자 6자리여야 합니다.")
    private String phoneCode;
}
