package com.deepdirect.deepwebide_be.member.controller;

import com.deepdirect.deepwebide_be.member.dto.response.SignInResponse;
import com.deepdirect.deepwebide_be.member.service.OAuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.PrintWriter;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class OAuthController {

    private final OAuthService oAuthService;

    @GetMapping("/github/callback")
    public void githubCallback(
            @RequestParam("code") String code,
            HttpServletResponse response) throws IOException {

        try {
            SignInResponse signInResponse = oAuthService.processGitHubLogin(code, response);
            sendSuccessResponse(response, signInResponse);
        } catch (Exception e) {
            sendErrorResponse(response, e.getMessage());
        }
    }

    private void sendSuccessResponse(HttpServletResponse response, SignInResponse data) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonData = objectMapper.writeValueAsString(data);

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();

        String html = String.format("""
            <!DOCTYPE html>
            <html>
            <head><title>GitHub Login Success</title></head>
            <body>
                <script>
                    console.log('GitHub 로그인 성공');
                    if (window.opener) {
                        window.opener.postMessage({
                            type: 'GITHUB_LOGIN_SUCCESS',
                            response: %s
                        }, 'http://localhost:5173');
                        window.close();
                    }
                </script>
                <p>로그인 성공! 창이 자동으로 닫힙니다.</p>
            </body>
            </html>
            """, jsonData);

        out.println(html);
        out.flush();
    }

    private void sendErrorResponse(HttpServletResponse response, String errorMessage) throws IOException {
        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();

        String html = String.format("""
            <script>
                if (window.opener) {
                    window.opener.postMessage({
                        type: 'GITHUB_LOGIN_ERROR',
                        error: '%s'
                    }, 'http://localhost:5173');
                    window.close();
                }
            </script>
            """, errorMessage);

        out.println(html);
        out.flush();
    }
}
