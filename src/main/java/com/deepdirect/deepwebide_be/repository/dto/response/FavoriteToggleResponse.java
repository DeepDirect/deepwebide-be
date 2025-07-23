package com.deepdirect.deepwebide_be.repository.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Schema(description = "즐겨찾기 토글 응답")
public class FavoriteToggleResponse {

    @Schema(description = "현재 즐겨찾기 상태", example = "true")
    private final boolean isFavorite;

    @Schema(description = "응답 메시지", example = "레포지토리가 즐겨찾기에 등록되었습니다.")
    private final String message;

    @Builder
    public FavoriteToggleResponse(boolean isFavorite, String message) {
        this.isFavorite = isFavorite;
        this.message = message;
    }

}
