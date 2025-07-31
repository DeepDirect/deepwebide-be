package com.deepdirect.deepwebide_be.member.controller;

import com.deepdirect.deepwebide_be.member.service.EmailVerificationService;
import com.deepdirect.deepwebide_be.member.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/email")
@Tag(name = "EmailVerification", description = "이메일 인증 관련 API")
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;
    private final UserService userService;

    @Operation(
            summary = "이메일 인증"
    )
    @GetMapping("/send-code")
    public RedirectView verifyEmail(@RequestParam String code) {
        boolean result = emailVerificationService.verifyEmailCode(code);
        String email = emailVerificationService.findVerifiedEmailByCode(code);

        if (result) {
            userService.setEmailVerificationService(email);
        }

        RedirectView redirectView = new RedirectView();
        redirectView.setUrl("https://www.deepdirect.site/sign-in");
        return redirectView;
    }
}
