package com.deepdirect.deepwebide_be.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PhoneVerifyCodeRequest {
    @NotBlank(message = "전화번호는 필수 입력입니다.")
    @Pattern(regexp = "^01[0-9]\\d{7,8}$", message = "올바른 형식의 전화번호여야 합니다.")
    private String phoneNumber;

    @NotBlank(message = "인증번호는 필수 입력입니다.")
    @Pattern(regexp = "^\\d{6}$", message = "인증번호는 숫자 6자리여야 합니다.")
    private String phoneCode;
}
