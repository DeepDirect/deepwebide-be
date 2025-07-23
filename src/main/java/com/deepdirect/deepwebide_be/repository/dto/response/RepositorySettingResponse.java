package com.deepdirect.deepwebide_be.repository.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@Schema(description = "레포지토리 환경설정 정보 응답")
public class RepositorySettingResponse {
    @Schema(description = "레포지토리 ID")
    private Long repositoryId;

    @Schema(description = "레포지토리 이름")
    private String repositoryName;

    @Schema(description = "레포 생성일", example = "2025-07-18T13:10:00Z")
    private LocalDateTime createdAt;

    @Schema(description = "레포 수정일", example = "2025-07-21T13:10:00Z")
    private LocalDateTime updatedAt;

    @Schema(description = "공유 여부")
    private boolean isShared;

    @Schema(description = "공유 링크", nullable = true)
    private String shareLink;

    @Schema(description = "레포 멤버 목록 (개인 레포는 빈 배열)")
    private List<MemberInfo> members;

    @Getter
    @Builder
    @Schema(description = "레포 멤버 정보")
    public static class MemberInfo {
        @Schema(description = "사용자 ID")
        private Long userId;

        @Schema(description = "닉네임")
        private String nickname;

        @Schema(description = "프로필 이미지 URL")
        private String profileImageUrl;

        @Schema(description = "레포 내 역할", example = "OWNER or MEMBER")
        private String role;
    }

}
