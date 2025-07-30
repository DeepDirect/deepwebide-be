package com.deepdirect.deepwebide_be.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "채팅 읽음 위치 저장 요청")
public class ChatReadOffsetRequest {

    @NotNull
    @Schema(description = "마지막으로 읽은 메시지 ID", example = "152")
    private Long lastReadMessageId;

    @Builder
    public ChatReadOffsetRequest(Long lastReadMessageId) {
        this.lastReadMessageId = lastReadMessageId;
    }
}
