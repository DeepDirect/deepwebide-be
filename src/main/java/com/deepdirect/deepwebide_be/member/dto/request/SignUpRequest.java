package com.deepdirect.deepwebide_be.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "회원가입 요청 DTO")
public class SignUpRequest {
    @NotBlank(message = "이메일 입력이 누락되었습니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    @Schema(description = "사용자 이메일", example = "test@test.com")
    private String email;

    @NotBlank(message = "이름 입력이 누락되었습니다.")
    @Schema(description = "사용자 이름", example = "홍길동")
    private String username;

    @NotBlank(message = "비밀번호 입력이 누락되었습니다.")
    @Schema(description = "비밀번호", example = "1Q2w3e4r!@#")
    private String password;

    @NotBlank(message = "비밀번호 확인 입력이 누락되었습니다.")
    @Schema(description = "비밀번호 확인", example = "1Q2w3e4r!@#")
    private String passwordCheck;

    @NotBlank(message = "전화번호 입력이 누락되었습니다.")
    @Schema(description = "전화번호", example = "01012345678")
    private String phoneNumber;
}
