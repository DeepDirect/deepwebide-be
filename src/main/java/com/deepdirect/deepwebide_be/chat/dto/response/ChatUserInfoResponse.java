package com.deepdirect.deepwebide_be.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "채팅방 사용자 정보 응답")
public class ChatUserInfoResponse {

    @Schema(description = "사용자 ID")
    private Long userId;

    @Schema(description = "닉네임")
    private String nickname;
}