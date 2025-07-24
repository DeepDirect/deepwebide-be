package com.deepdirect.deepwebide_be.repository.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
@Schema(description = "레포지토리 이름 변경 요청 DTO")
public class RepositoryRenameRequest {

    @NotBlank
    @Size(max = 50, message = "레포지토리 이름은 50자 이하여야 합니다.")
    @Schema(description = "레포지토리 이름", example = "deepwebide-refactored")
    private String repositoryName;
}