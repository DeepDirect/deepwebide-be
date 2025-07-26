package com.deepdirect.deepwebide_be.member.controller;

import com.deepdirect.deepwebide_be.global.dto.ApiResponseDto;
import com.deepdirect.deepwebide_be.member.dto.request.*;
import com.deepdirect.deepwebide_be.member.dto.response.*;
import com.deepdirect.deepwebide_be.member.service.TokenService;
import com.deepdirect.deepwebide_be.member.service.UserService;
import com.deepdirect.deepwebide_be.sentry.SentryUserContextService;
import io.sentry.Sentry;
import io.sentry.SentryLevel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(
        origins = {
                "http://localhost:5173",
                "https://www.deepwebide.site"
        },
        allowCredentials = "true"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "User", description = "회원가입, 로그인, 비밀번호 재설정 등 사용자 인증 API")
public class UserController {

    private final UserService userService;
    private final TokenService tokenService;
    private final SentryUserContextService sentryUserContextService;

    @Operation(summary = "회원가입")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponseDto<SignUpResponse>> signUp(@Valid @RequestBody SignUpRequest signUpRequest) {
        SignUpResponse response = userService.signup(signUpRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.of(201, "회원가입이 완료되었습니다.", response));
    }

    @Operation(summary = "로그인")
    @PostMapping("/signin")
    public ResponseEntity<ApiResponseDto<SignInResponse>> signIn(
            @Valid @RequestBody SignInRequest signInRequest,
            HttpServletResponse servletResponse
    ) {
        // 1. 서비스에서 로그인 + 토큰 2개 발급
        SignInResponse response = userService.signIn(signInRequest, servletResponse);

        // ★ Sentry Scope에 유저 정보 세팅
        sentryUserContextService.setCurrentUserContext();

        // ★ Sentry 메시지로 로그인 이벤트 기록
        Sentry.captureMessage(
                "로그인: " + response.getUser().getUsername() + ": " + response.getUser().getNickname(),
                SentryLevel.INFO
        );

        // response(본문)는 AccessToken만, RefreshToken은 쿠키로 헤더에 내려감!
        return ResponseEntity.ok(ApiResponseDto.of(200, "로그인에 성공했습니다.", response));
    }

    @Operation(summary = "로그아웃",security = @SecurityRequirement(name = "Authorization"))
    @PostMapping("/signout")
    public ResponseEntity<ApiResponseDto<Void>> signOut(
            @RequestHeader("Authorization") String authorizationHeader,
            HttpServletResponse response
    ) {
        userService.signOut(authorizationHeader, response);

        // ★ Sentry 메시지로 로그인 이벤트 기록
        Sentry.captureMessage(
                "로그아웃", SentryLevel.INFO);


        // ★ Sentry Scope에서 유저 정보 제거
        sentryUserContextService.clearUserContext();

        return ResponseEntity.ok(ApiResponseDto.of(200, "로그아웃 되었습니다.", null));
    }

    @Operation(summary = "이메일(아이디) 찾기")
    @PostMapping("/email/find")
    public ResponseEntity<ApiResponseDto<FindEmailResponse>> findEmail(@Valid @RequestBody FindEmailRequest request) {
        FindEmailResponse response = new FindEmailResponse(userService.findEmail(request));
        return ResponseEntity.ok(ApiResponseDto.of(200, "이메일(아이디)을 찾았습니다.", response));
    }

    @Operation(summary = "이메일 중복 확인")
    @PostMapping("/email/check")
    public ResponseEntity<ApiResponseDto<EmailCheckResponse>> checkEmail(
            @Valid @RequestBody EmailCheckRequest emailCheckRequest) {
        boolean isAvailable = userService.isEmailAlreadyExist(emailCheckRequest.getEmail());

        EmailCheckResponse emailCheckResponse = new EmailCheckResponse(isAvailable);

        return ResponseEntity.ok(ApiResponseDto.of(
                200, "사용 가능한 이메일입니다.", emailCheckResponse
        ));
    }

    @Operation(summary = "비밀번호 변경 전 본인 인증")
    @PostMapping("/password/verify-user")
    public ResponseEntity<ApiResponseDto<PasswordVerifyUserResponse>> verifyUser(@Valid @RequestBody PasswordVerifyUserRequest request) {
        String reauthToken = userService.passwordVerifyUser(request);
        PasswordVerifyUserResponse response = new PasswordVerifyUserResponse(reauthToken);
        return ResponseEntity.ok(ApiResponseDto.of(200, "본인 인증에 성공했습니다.", response));
    }

    @Operation(summary = "비밀번호 재설정", security = @SecurityRequirement(name = "Authorization"))
    @PostMapping("/password/reset")
    public ResponseEntity<ApiResponseDto<Void>> resetPassword(
            @Valid @RequestBody PasswordResetRequest request,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        userService.verifyAndResetPassword(request, authorizationHeader);
        return ResponseEntity.ok(ApiResponseDto.of(200, "비밀번호가 재설정되었습니다.", null));
    }

    @Operation(summary = "AccessToken 재발급")
    @PostMapping("/token")
    public ResponseEntity<ApiResponseDto<TokenResponse>> reissueAccessToken(
            @CookieValue("refreshToken") String refreshToken
    ) {
        String result = tokenService.reissueAccessToken(refreshToken);
        TokenResponse response = new TokenResponse(result);
        return ResponseEntity.ok(ApiResponseDto.of(200, "토큰 재발급에 성공했습니다.", response));
    }
}
