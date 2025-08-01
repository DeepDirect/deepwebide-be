package com.deepdirect.deepwebide_be.chat.dto.response;

import com.deepdirect.deepwebide_be.chat.domain.ChatMessage;
import com.deepdirect.deepwebide_be.chat.domain.ChatMessageType;
import com.deepdirect.deepwebide_be.member.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "채팅 브로드캐스트 메시지 응답")
public class ChatMessageBroadcast {

    @Schema(description = "레포지토리 ID")
    private Long repositoryId;

    @Schema(description = "메시지 타입", example = "CHAT")
    private ChatMessageType type;

    @Schema(description = "메시지 ID")
    private Long messageId;

    @Schema(description = "보낸 사용자 ID")
    private Long senderId;

    @Schema(description = "보낸 사용자 닉네임")
    private String senderNickname;

    @Schema(description = "보낸 사용자 프로필 이미지 URL")
    private String senderProfileImageUrl;

    @Schema(description = "메시지 내용")
    private String message;

    @Schema(description = "코드 참조 정보", nullable = true)
    private CodeReferenceResponse codeReference;

    @Schema(description = "보낸 시간")
    private LocalDateTime sentAt;

    @Schema(description = "내 메시지 여부", name = "IsMine")
    private boolean isMine;


    public static ChatMessageBroadcast of(ChatMessage message, User sender, Long repositoryId, CodeReferenceResponse codeReference) {
        return ChatMessageBroadcast.builder()
                .repositoryId(repositoryId)
                .type(ChatMessageType.CHAT) //일반 채팅
                .messageId(message.getId())
                .senderId(sender.getId())
                .senderNickname(sender.getNickname())
                .senderProfileImageUrl(sender.getProfileImageUrl())
                .message(message.getMessage())
                .codeReference(codeReference)
                .sentAt(message.getSentAt())
                .isMine(false)
                .build();
    }

    public static ChatMessageBroadcast system(User user, ChatMessageType type, String message, Long repositoryId) {
        return ChatMessageBroadcast.builder()
                .repositoryId(repositoryId)
                .type(type)
                .messageId(null)
                .senderId(user.getId())
                .senderNickname(user.getNickname())
                .senderProfileImageUrl(user.getProfileImageUrl())
                .message(message)
                .codeReference(null)
                .sentAt(LocalDateTime.now())
                .isMine(false)
                .build();
    }
}