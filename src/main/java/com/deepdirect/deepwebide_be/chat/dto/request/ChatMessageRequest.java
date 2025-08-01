package com.deepdirect.deepwebide_be.chat.dto.request;

import com.deepdirect.deepwebide_be.chat.domain.ChatMessageType;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "채팅 메시지 전송 요청")
public class ChatMessageRequest {

    @NotBlank(message = "메시지는 공백일 수 없습니다.")
    @Schema(description = "메시지 내용", example = "코드 이부분 이상한 것 같아")
    private String message;

    @Schema(description = "메시지 타입", example = "CHAT")
    @JsonProperty("type")
    private ChatMessageType type;

    @Schema(description = "레포지토리 ID")
    private Long repositoryId;

    @Schema(description = "코드 참조 정보")
    private CodeReferenceRequest codeReference;

    @Builder
    public ChatMessageRequest(String message, ChatMessageType type, Long repositoryId, CodeReferenceRequest codeReference) {
        this.message = message;
        this.type = type;
        this.repositoryId = repositoryId;
        this.codeReference = codeReference;
    }
}
