package com.deepdirect.deepwebide_be.repository.controller;

import com.deepdirect.deepwebide_be.global.dto.ApiResponseDto;
import com.deepdirect.deepwebide_be.global.security.CustomUserDetails;
import com.deepdirect.deepwebide_be.repository.dto.response.FavoriteToggleResponse;
import com.deepdirect.deepwebide_be.repository.service.RepositoryFavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/repositories")
@Tag(name = "Favorite", description = "레포지토리 즐겨찾기 등록/취소 API")
public class RepositoryFavoriteController {
    private final RepositoryFavoriteService repositoryFavoriteService;

    @PostMapping("/{repositoryId}/favorite")
    @Operation(
            summary = "레포지토리 즐겨찾기 등록/취소",
            description = "레포지토리에 대해 즐겨찾기 등록 또는 취소를 토글합니다."
    )
    public ResponseEntity<ApiResponseDto<FavoriteToggleResponse>> toggleFavorite(
            @PathVariable Long repositoryId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        FavoriteToggleResponse response =
                repositoryFavoriteService.toggleFavorite(repositoryId, userDetails.getId());

        return ResponseEntity.ok(ApiResponseDto.of(
                200,
                response.getMessage(),
                null
        ));
    }
}
