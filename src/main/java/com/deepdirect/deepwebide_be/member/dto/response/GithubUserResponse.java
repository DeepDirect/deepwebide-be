package com.deepdirect.deepwebide_be.member.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GithubUserResponse {
    private long id;
    private String login;
    private String name;
    private String email;

    @JsonProperty("avatar_url")
    private String avatarUrl;
}
