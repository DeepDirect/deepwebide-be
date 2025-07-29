package com.deepdirect.deepwebide_be.repository.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "레포지토리 실행 결과 응답 DTO")
public class RepositoryExecuteResponse {

    @Schema(description = "레포지토리 UUID", example = "123e4567-e89b-12d3-a456-426614174000")
    private String uuid;

    @Schema(description = "S3 URL", example = "https://s3.amazonaws.com/bucket/123e4567-e89b-12d3-a456-426614174000.zip")
    private String s3Url;

    @Schema(description = "레포지토리 포트 번호", example = "8080")
    private Integer port;

    @Schema(description = "실행 결과 메시지", example = "레포지토리 실행이 성공적으로 완료되었습니다.")
    private String message;

    // 추가된 필드들
    @Schema(description = "실행 ID", example = "exec-123456")
    private String executionId;

    @Schema(description = "실행 상태", example = "RUNNING", allowableValues = {"PENDING", "RUNNING", "SUCCESS", "FAILED"})
    private String status;

    @Schema(description = "실행 출력", example = "Application started successfully on port 8080")
    private String output;

    @Schema(description = "실행 에러", example = "")
    private String error;

    @Schema(description = "실행 시간 (밀리초)", example = "5000")
    private Long executionTime;
}
