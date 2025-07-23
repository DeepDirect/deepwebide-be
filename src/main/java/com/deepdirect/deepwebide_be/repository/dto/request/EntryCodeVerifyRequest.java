package com.deepdirect.deepwebide_be.repository.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "레포지토리 입장 코드 검증 요청 DTO")
public class EntryCodeVerifyRequest {

    @NotBlank
    @Schema(description = "입장코드", example = "Q3irtr53")
    private String entryCode;
}
