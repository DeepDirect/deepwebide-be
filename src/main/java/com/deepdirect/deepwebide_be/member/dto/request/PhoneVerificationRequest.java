package com.deepdirect.deepwebide_be.member.dto.request;

import com.deepdirect.deepwebide_be.member.domain.AuthType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "휴대폰 번호 인증 발송 DTO")
public class PhoneVerificationRequest {

    @NotBlank(message = "전화번호 입력이 누락되었습니다.")
    @Schema(description = "전화번호", example = "01012345678")
    private String phoneNumber;

    @NotBlank(message = "이름 입력이 누락되었습니다.")
    @Schema(description = "사용자 이름", example = "홍길동")
    private String username;

    @NotNull(message = "인증 타입이 누락되었습니다.")
    @Schema(description = "인증 타입", example = "SIGN_UP / FIND_ID / FIND_PASSWORD")
    private AuthType authType;
}
