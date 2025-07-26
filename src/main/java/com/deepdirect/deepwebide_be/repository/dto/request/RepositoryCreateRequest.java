package com.deepdirect.deepwebide_be.repository.dto.request;

import com.deepdirect.deepwebide_be.repository.domain.RepositoryType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "레포지토리 생성 요청 DTO")
public class RepositoryCreateRequest {

    public static final String REPO_NAME_REGEX = "^(?![-_])[a-zA-Z0-9가-힣-_]+$";

    @NotBlank
    @Size(max = 50, message = "레포지토리 이름은 50자 이하여야 합니다.")
    @Pattern(regexp = REPO_NAME_REGEX, message = "레포지토리 이름은 영어, 한글, 숫자, '-', '_'만 사용 가능하며, '-'나 '_'로 시작할 수 없습니다.")
    @Schema(description = "레포지토리 이름", example = "deepwebide")
    private String repositoryName;

    @NotNull
    @Schema(description = "레포지토리 타입", example = "SPRING_BOOT")
    private RepositoryType repositoryType;
}
