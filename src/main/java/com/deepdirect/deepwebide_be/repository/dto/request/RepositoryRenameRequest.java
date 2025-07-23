package com.deepdirect.deepwebide_be.repository.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class RepositoryRenameRequest {

    @NotBlank
    @Size(max = 50, message = "레포지토리 이름은 50자 이하여야 합니다.")
    private String repositoryName;
}