package com.deepdirect.deepwebide_be.sandbox.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SandboxExecutionRequest {
    private String uuid;
    private String url;
    private String framework;
    private Integer port;
}
