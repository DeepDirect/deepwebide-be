package com.deepdirect.deepwebide_be.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "채팅 보낸 사용자 정보")
public class SenderInfo {

    @Schema(description = "보낸 사용자 ID")
    private final Long userId;

    @Schema(description = "닉네임")
    private final String nickname;

    @Schema(description = "프로필 이미지 URL", nullable = true)
    private final String profileImageUrl;
}
