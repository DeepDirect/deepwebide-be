package com.deepdirect.deepwebide_be.repository.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "추방하기 응답 DTO")
public class KickedMemberResponse {

    @Schema(description = "추방된 멤버 Id", example = "1")
    private Long kickedUserId;
}