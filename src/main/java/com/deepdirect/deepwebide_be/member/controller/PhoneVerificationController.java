package com.deepdirect.deepwebide_be.member.controller;

import com.deepdirect.deepwebide_be.global.dto.ApiResponseDto;
import com.deepdirect.deepwebide_be.member.dto.request.PhoneVerificationRequest;
import com.deepdirect.deepwebide_be.member.dto.request.PhoneVerifyCodeRequest;
import com.deepdirect.deepwebide_be.member.dto.response.PhoneVerificationResponse;
import com.deepdirect.deepwebide_be.member.dto.response.PhoneVerifyCodeResponse;
import com.deepdirect.deepwebide_be.member.service.PhoneVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/phone")
@Tag(name = "PhoneVerification", description = "전화번호 인증 관련 API")
public class PhoneVerificationController {
    private final PhoneVerificationService phoneVerificationService;

    @Operation(
            summary = "전화번호 인증"
    )
    @PostMapping("/send-code")
    public ResponseEntity<ApiResponseDto<PhoneVerificationResponse>> sendCode(
            @Valid @RequestBody PhoneVerificationRequest request) {

        int time = phoneVerificationService.sendVerificationCode(
                request.getPhoneNumber(),
                request.getUsername(),
                request.getAuthType()
        );

        PhoneVerificationResponse expiresIn = new PhoneVerificationResponse(time);

        return ResponseEntity.ok(ApiResponseDto.of(200, "인증번호가 발송되었습니다.", expiresIn));
    }

    @Operation(
            summary = "전화번호 인증번호 확인"
    )
    @PostMapping("/verify-code")
    public ResponseEntity<ApiResponseDto<PhoneVerifyCodeResponse>> verifyCode(
            @Valid @RequestBody PhoneVerifyCodeRequest  request) {

        boolean verificationResult = phoneVerificationService.verifyCode(request.getPhoneNumber(), request.getPhoneCode());

        PhoneVerifyCodeResponse verificationResponse = new PhoneVerifyCodeResponse(verificationResult);

        return ResponseEntity.ok(ApiResponseDto.of(200, "인증에 성공했습니다.", verificationResponse));
    }

}
