package com.deepdirect.deepwebide_be.member.controller;

import com.deepdirect.deepwebide_be.global.dto.ApiResponseDto;
import com.deepdirect.deepwebide_be.member.dto.request.*;
import com.deepdirect.deepwebide_be.member.dto.response.*;
import com.deepdirect.deepwebide_be.member.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(
    origins = {
        "http://localhost:5173",
    },
    allowCredentials = "true"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponseDto<SignUpResponse>> signUp(@Valid @RequestBody SignUpRequest signUpRequest) {
        SignUpResponse response = userService.signup(signUpRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.of(201, "회원가입이 완료되었습니다.", response));
    }

    @PostMapping("/signin")
    public ResponseEntity<ApiResponseDto<SignInResponse>> signIn(
            @Valid @RequestBody SignInRequest signInRequest,
            HttpServletResponse servletResponse
    ) {
        // 1. 서비스에서 로그인 + 토큰 2개 발급
        SignInResponse response = userService.signIn(signInRequest, servletResponse);
        // response(본문)는 AccessToken만, RefreshToken은 쿠키로 헤더에 내려감!
        return ResponseEntity.ok(ApiResponseDto.of(200, "로그인에 성공했습니다.", response));
    }

    @PostMapping("/signout")
    public ResponseEntity<ApiResponseDto<Void>> signOut(
            @RequestHeader("Authorization") String authorizationHeader,
            HttpServletResponse response
    ) {
        userService.signOut(authorizationHeader, response);
        return ResponseEntity.ok(ApiResponseDto.of(200, "로그아웃 되었습니다.", null));
    }

    @PostMapping("/email/find")
    public ResponseEntity<ApiResponseDto<FindEmailResponse>> findEmail(@Valid @RequestBody FindEmailRequest request) {
        FindEmailResponse response = new FindEmailResponse(userService.findEmail(request));
        return ResponseEntity.ok(ApiResponseDto.of(200, "이메일(아이디)을 찾았습니다.", response));
    }

    @PostMapping("/email/check")
    public ResponseEntity<ApiResponseDto<EmailCheckResponse>> checkEmail(
            @Valid @RequestBody EmailCheckRequest emailCheckRequest) {
        boolean isAvailable = userService.isEmailAlreadyExist(emailCheckRequest.getEmail());

        EmailCheckResponse emailCheckResponse = new EmailCheckResponse(isAvailable);

        return ResponseEntity.ok(ApiResponseDto.of(
                200, "사용 가능한 이메일입니다.", emailCheckResponse
        ));
    }

    @PostMapping("/password/verify-user")
    public ResponseEntity<ApiResponseDto<PasswordVerifyUserResponse>> verifyUser(@Valid @RequestBody PasswordVerifyUserRequest request) {
        String reauthToken = userService.passwordVerifyUser(request);
        PasswordVerifyUserResponse response = new PasswordVerifyUserResponse(reauthToken);
        return ResponseEntity.ok(ApiResponseDto.of(200, "본인 인증에 성공했습니다.", response));
    }

    @PostMapping("/password/reset")
    public ResponseEntity<ApiResponseDto<Void>> resetPassword(
            @Valid @RequestBody PasswordResetRequest request,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        userService.verifyAndResetPassword(request, authorizationHeader);
        return ResponseEntity.ok(ApiResponseDto.of(200, "비밀번호가 재설정되었습니다.", null));
    }
}
