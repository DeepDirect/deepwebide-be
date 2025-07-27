package com.deepdirect.deepwebide_be.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "채팅 메시지 리스트 응답")
public class ChatMessagesResponse {

    @Schema(description = "더 불러올 메시지 존재 여부", example = "true")
    private final boolean hasMore;

    @Schema(description = "채팅 메시지 목록")
    private final List<ChatMessageResponse> messages;
}