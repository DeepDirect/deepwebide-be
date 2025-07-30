package com.deepdirect.deepwebide_be.chat.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "채팅 메시지 타입")
public enum ChatMessageType {
    CHAT,           // 일반 채팅 메시지
    USER_JOINED,    // 입장
    USER_LEFT       // 퇴장
    ;

    @JsonCreator // 문자열 → Enum
    public static ChatMessageType from(String value) {
        return ChatMessageType.valueOf(value.toUpperCase()); // "chat" → "CHAT"
    }

    @JsonValue // Enum → 문자열
    public String toValue() {
        return this.name();
    }
}
