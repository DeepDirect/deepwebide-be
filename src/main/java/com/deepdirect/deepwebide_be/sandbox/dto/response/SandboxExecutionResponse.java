package com.deepdirect.deepwebide_be.sandbox.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SandboxExecutionResponse {
    private String executionId;
    private String status;
    private String output;
    private String error;
    private Long executionTime;
    private String message;
}