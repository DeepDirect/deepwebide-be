package com.deepdirect.deepwebide_be.member.controller;

import com.deepdirect.deepwebide_be.member.dto.response.SignInResponse;
import com.deepdirect.deepwebide_be.member.service.OAuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class OAuthController {

    private final OAuthService oAuthService;

    @GetMapping("/github/callback")
    public SignInResponse githubCallback(
            @RequestParam("code") String code,
            HttpServletResponse response) {
        return oAuthService.processGitHubLogin(code, response);
    }
}