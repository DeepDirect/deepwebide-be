package com.deepdirect.deepwebide_be.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "채팅 메시지 리스트 응답")
public class ChatMessagesResponse {

    @Schema(description = "채팅 메시지 목록")
    private final List<ChatMessageResponse> messages;

    public ChatMessagesResponse(List<ChatMessageResponse> messages) {
        this.messages = messages;
    }

}