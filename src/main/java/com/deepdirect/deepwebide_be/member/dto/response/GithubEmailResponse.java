package com.deepdirect.deepwebide_be.member.dto.response;

import lombok.Data;

@Data
public class GithubEmailResponse {
    private String email;
    private Boolean primary;
    private Boolean verified;
    private String visibility;
}
