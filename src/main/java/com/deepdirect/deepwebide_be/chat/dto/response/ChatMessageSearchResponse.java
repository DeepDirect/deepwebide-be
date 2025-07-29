package com.deepdirect.deepwebide_be.chat.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ChatMessageSearchResponse {
    private String keyword;
    private long totalElements;
    private List<ChatMessageResponse> messages;
}
