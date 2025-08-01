package com.deepdirect.deepwebide_be.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "채팅 메시지 응답")
public class ChatMessageResponse {

    @Schema(description = "메시지 ID")
    private final Long messageId;

    @Schema(description = "보낸 사람 ID")
    private final Long senderId;

    @Schema(description = "보낸 사람 닉네임")
    private final String senderNickname;

    @Schema(description = "보낸 사람 프로필 이미지")
    private final String senderProfileImageUrl;

    @Schema(description = "메시지 본문")
    private final String message;

    @Schema(description = "코드 참조")
    private final CodeReferenceResponse codeReference;

    @Schema(description = "내 메시지 여부", name = "IsMine")
    private final boolean isMine;

    @Schema(description = "보낸 시간", example = "2025-07-25T21:20:00Z")
    private final LocalDateTime sentAt;
}