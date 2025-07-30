package com.deepdirect.deepwebide_be.chat.dto.response;

import com.deepdirect.deepwebide_be.chat.domain.ChatMessageType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "채팅 시스템 메시지 응답")
public class ChatSystemMessageResponse {

    @Schema(description = "메시지 타입", example = "USER_JOINED")
    private final ChatMessageType type;

    @Schema(description = "시스템 메시지 내용", example = "자유로운 개발자님이 입장하였습니다.")
    private final String message;

    @Schema(description = "레포지토리 ID", example = "1")
    private final Long repositoryId;

    @Schema(description = "사용자 ID", example = "1")
    private final Long userId;

    @Schema(description = "닉네임", example = "자유로운 개발자")
    private final String nickname;

    @Schema(description = "프로필 이미지 URL", nullable = true)
    private final String profileImageUrl;

    @Schema(description = "현재 접속 인원 수", example = "3")
    private final int connectedCount;

    @Schema(description = "메시지 전송 시각", example = "2025-07-30T13:40:00")
    private final LocalDateTime sentAt;

    public static ChatSystemMessageResponse enter(Long repositoryId,Long userId, String nickname, String profileImageUrl, long count) {
        return ChatSystemMessageResponse.builder()
                .repositoryId(repositoryId)
                .type(ChatMessageType.USER_JOINED)
                .message(nickname + "님이 입장하였습니다.")
                .userId(userId)
                .nickname(nickname)
                .profileImageUrl(profileImageUrl)
                .connectedCount((int) count)
                .sentAt(LocalDateTime.now())
                .build();
    }

    public static ChatSystemMessageResponse exit(Long repositoryId, Long userId, String nickname, long count) {
        return ChatSystemMessageResponse.builder()
                .repositoryId(repositoryId)
                .type(ChatMessageType.USER_LEFT)
                .message(nickname + "님이 퇴장하였습니다.")
                .userId(userId)
                .nickname(nickname)
                .profileImageUrl(null)
                .connectedCount((int) count)
                .sentAt(LocalDateTime.now())
                .build();
    }
}
