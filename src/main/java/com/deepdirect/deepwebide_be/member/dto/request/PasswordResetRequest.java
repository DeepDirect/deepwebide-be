package com.deepdirect.deepwebide_be.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "비밀번호 변경")
public class PasswordResetRequest {

    @NotBlank(message = "비밀번호 입력이 누락되었습니다.")
    @Schema(description = "새로운 비밀번호", example = "!@#123qweR")
    private String newPassword;

    @NotBlank(message = "비밀번호 확인 입력이 누락되었습니다.")
    @Schema(description = "새로운 비밀번호", example = "!@#123qweR")
    private String passwordCheck;
}
