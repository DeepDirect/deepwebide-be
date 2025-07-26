package com.deepdirect.deepwebide_be.repository.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "레포지토리의 개발 환경 타입")
public enum RepositoryType {
    SPRING_BOOT, REACT, FAST_API
}