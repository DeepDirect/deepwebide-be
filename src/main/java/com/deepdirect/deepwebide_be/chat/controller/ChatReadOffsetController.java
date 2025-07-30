package com.deepdirect.deepwebide_be.chat.controller;

import com.deepdirect.deepwebide_be.chat.dto.request.ChatReadOffsetRequest;
import com.deepdirect.deepwebide_be.chat.service.ChatReadOffsetService;
import com.deepdirect.deepwebide_be.global.dto.ApiResponseDto;
import com.deepdirect.deepwebide_be.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/repositories/{repositoryId}/chat")
public class ChatReadOffsetController {

    private final ChatReadOffsetService chatReadOffsetService;

    @PutMapping("/read-offset")
    public ResponseEntity<ApiResponseDto<Void>> saveReadOffset(
            @PathVariable Long repositoryId,
            @Valid @RequestBody ChatReadOffsetRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        chatReadOffsetService.saveOffset(repositoryId, userDetails.getId(), request.getLastReadMessageId());
        return ResponseEntity.ok(ApiResponseDto.of(200, "읽음 처리 완료", null));
    }
}
