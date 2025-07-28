package com.deepdirect.deepwebide_be.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "채팅방 사용자 입퇴장 메시지 응답")
public class ChatUserEventResponse {

    @Schema(description = "이벤트 타입", example = "USER_JOINED or USER_LEFT")
    private String type;

    @Schema(description = "레포지토리 ID")
    private Long repositoryId;

    @Schema(description = "사용자 정보")
    private ChatUserInfoResponse user;

    @Schema(description = "현재 접속자 수")
    private int activeUserCount;

    @Schema(description = "메시지")
    private String message;

    @Schema(description = "이벤트 발생 시간", example = "2025-07-28T12:05:00Z")
    private LocalDateTime timestamp;
}