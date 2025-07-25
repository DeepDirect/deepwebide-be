package com.deepdirect.deepwebide_be.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "이메일 중복 확인")
public class EmailCheckRequest {

    @NotBlank
    @Email
    @Schema(description = "가입할 이메일", example = "test@test.com")
    private String email;
}
