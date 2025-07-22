package com.deepdirect.deepwebide_be.member.controller;

import com.deepdirect.deepwebide_be.member.service.EmailVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(
        origins = {
                "http://localhost:5173",
        },
        allowCredentials = "true"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/email")
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    @Operation(
            summary = "이메일 인증"
    )
    @GetMapping("/send-code")
    public ResponseEntity<Map<String, Object>> verifyEmail(@RequestBody String code) {
        boolean result = emailVerificationService.verifyEmailCode(code);
        Map<String, Object> response = new HashMap<>();

        // TODO: 리다이랙트 넣기~
        if (result) {
            response.put("success", true);
            response.put("message", "이메일 인증이 완료되었습니다.");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "인증 코드가 만료되었거나 유효하지 않습니다.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}
