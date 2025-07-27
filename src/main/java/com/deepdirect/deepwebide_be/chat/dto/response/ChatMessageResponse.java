package com.deepdirect.deepwebide_be.chat.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "채팅 메시지 응답")
public class ChatMessageResponse {

    @Schema(description = "채팅 메시지 ID")
    private final Long id;

    @Schema(description = "보낸 사람 정보")
    private final SenderInfo sender;

    @Schema(description = "메시지 내용", example = "안녕하세요~!")
    private final String message;

    @Schema(description = "전송 일시", example = "2025-07-27T22:00:00Z")
    private final LocalDateTime sentAt;

    @JsonProperty("is_referenced")
    @Schema(description = "코드 참조 여부", name = "is_referenced", example = "true")
    private final boolean isReferenced;

    @Schema(description = "참조된 파일 경로", example = "src/components/Editor.ts")
    private final String filePath;

    @Schema(description = "참조된 줄 번호", example = "32")
    private final Integer lineNumber;



}