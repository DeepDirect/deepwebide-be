package com.deepdirect.deepwebide_be.chat.controller;

import com.deepdirect.deepwebide_be.chat.dto.response.ChatMessageSearchResponse;
import com.deepdirect.deepwebide_be.chat.dto.response.ChatMessagesResponse;
import com.deepdirect.deepwebide_be.chat.dto.response.CodePathListResponse;
import com.deepdirect.deepwebide_be.chat.service.ChatMessageService;
import com.deepdirect.deepwebide_be.global.dto.ApiResponseDto;
import com.deepdirect.deepwebide_be.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/repositories/{repositoryId}/chat")
public class ChatController {

    private final ChatMessageService chatMessageService;

    @Operation(summary = "채팅 메시지 조회", description = "레포지토리에 참여한 사용자가 채팅 메시지를 조회합니다.")
    @GetMapping("/messages")
    public ResponseEntity<ApiResponseDto<ChatMessagesResponse>> getMessages(
            @PathVariable Long repositoryId,
            @RequestParam(required = false) Long before,
            @RequestParam(required = false) Long after,
            @RequestParam(defaultValue = "20") Integer size,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getId();
        ChatMessagesResponse response = chatMessageService.getMessages(repositoryId, userId, before, after, size);
        String message = (before != null)
                ? "과거 채팅 메시지 조회에 성공했습니다."
                : "채팅 메시지 조회에 성공했습니다.";
        return ResponseEntity.ok(ApiResponseDto.of(200, message, response));
    }
    @Operation(summary = "코드 참조 파일 경로 조회", description = "레포지토리 내 전체 파일 경로를 반환합니다.")
    @GetMapping("/code-paths")
    public ResponseEntity<ApiResponseDto<CodePathListResponse>> getCodePaths(
            @PathVariable Long repositoryId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        CodePathListResponse response = chatMessageService.getCodePaths(repositoryId, userDetails.getId());
        return ResponseEntity.ok(ApiResponseDto.of(200, "코드 참조 파일 경로 조회에 성공했습니다.", response));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponseDto<ChatMessageSearchResponse>> search(
            @PathVariable Long repositoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        ChatMessageSearchResponse response = chatMessageService.searchMessages(repositoryId, userDetails.getId(), keyword, size);
        return ResponseEntity.ok(ApiResponseDto.of(200,"채팅 메시지 검색 결과입니다.",response));
    }

}
