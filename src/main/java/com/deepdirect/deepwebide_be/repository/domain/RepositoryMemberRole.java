package com.deepdirect.deepwebide_be.repository.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "레포지토리 내 사용자 권한")
public enum RepositoryMemberRole {

    OWNER, MEMBER
}
